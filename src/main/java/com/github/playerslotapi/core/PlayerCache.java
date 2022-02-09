package com.github.playerslotapi.core;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.listener.VanillaListener;
import com.github.playerslotapi.slot.AbstractSlot;
import com.github.playerslotapi.slot.impl.VanillaSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PlayerCache {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    /**
     * 需要缓存的槽位
     */
    private static final Set<AbstractSlot> REGISTERED_SLOTS = new HashSet<>();
    private static final Map<Class<?>, Function<ItemStack, ?>> DATA_READER = new ConcurrentHashMap<>();
    private final Map<AbstractSlot, ItemStack> itemCache = new ConcurrentHashMap<>();
    private final Map<AbstractSlot, Map<Class<?>, Object>> dataCache = new ConcurrentHashMap<>();
    private final Player player;

    public PlayerCache(Player player) {
        this.player = player;
        for (AbstractSlot slot : REGISTERED_SLOTS) {
            ItemStack item = slot.get(player);
            itemCache.put(slot, item == null ? AIR : item);
            if (!DATA_READER.isEmpty()) {
                dataCache.put(slot, new ConcurrentHashMap<>());
            }
        }
    }

    /**
     * 槽位注册
     *
     * @param slot 要注册的槽位类型
     */
    public static void registerSlot(AbstractSlot slot) {
        REGISTERED_SLOTS.add(slot);
    }

    /**
     * 注册装备信息读取器
     * 读取器负责从装备上读取出指定的信息
     * 装备更新时, 读取器将被异步调用
     *
     * @param clazz  信息类型
     * @param reader 读取器
     */
    public static <T> void registerDataReader(Class<T> clazz, Function<ItemStack, T> reader) {
        DATA_READER.put(clazz, reader);
    }

    /**
     * 注册原版槽位
     */
    public static void registerVanilla() {
        registerSlot(VanillaSlot.MAINHAND);
        registerSlot(VanillaSlot.OFFHAND);
        registerSlot(VanillaSlot.HELMET);
        registerSlot(VanillaSlot.CHESTPLATE);
        registerSlot(VanillaSlot.LEGGINGS);
        registerSlot(VanillaSlot.BOOTS);
        VanillaListener.registerEvents();
    }

    /**
     * 获取所有已经注册的槽位
     *
     * @return
     */

    public static Set<AbstractSlot> getRegisteredSlots() {
        return REGISTERED_SLOTS;
    }

    /**
     * 获取槽位中缓存的物品
     *
     * @param slot 要获取的槽位
     * @return 缓存物品
     */
    @NotNull
    public ItemStack getCachedItem(AbstractSlot slot) {
        return itemCache.getOrDefault(slot, AIR);
    }

    /**
     * 获取槽位中缓存的装备信息
     *
     * @param slot  槽位
     * @param clazz 信息类型
     * @return 装备上的信息类实例
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getCachedData(AbstractSlot slot, Class<T> clazz) {
        return (T) dataCache.get(slot).get(clazz);
    }

    /**
     * 尝试更新槽位物品
     * 注意这个方法会被频繁调用
     *
     * @param slot 槽位类型
     * @param item 槽位物品
     */
    public void updateCachedItem(AbstractSlot slot, ItemStack item) {
        ItemStack old = itemCache.get(slot);
        if (item.equals(old)) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(PlayerSlotAPI.getPlugin(), () -> {
            AsyncSlotUpdateEvent event = new AsyncSlotUpdateEvent(player, slot, old, item);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            itemCache.put(slot, event.getNewItem());
            // 当信息读取器不为空时, 从装备上读取信息并丢入Map中
            Map<Class<?>, Object> data = dataCache.get(slot);
            for (Map.Entry<Class<?>, Function<ItemStack, ?>> entry : DATA_READER.entrySet()) {
                Object info = entry.getValue().apply(event.getNewItem());
                if (info != null) {
                    data.put(entry.getKey(), info);
                } else {
                    data.remove(entry.getKey());
                }
            }
        });
    }

    /**
     * 更新所有槽位
     */
    public void updateAll() {
        for (AbstractSlot slot : REGISTERED_SLOTS) {
            updateCachedItem(slot, slot.get(player));
        }
    }

}
