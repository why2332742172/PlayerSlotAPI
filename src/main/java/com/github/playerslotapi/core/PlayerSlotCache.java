package com.github.playerslotapi.core;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.event.UpdateTrigger;
import com.github.playerslotapi.slot.PlayerSlot;
import com.github.playerslotapi.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerSlotCache {


    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private final PlayerSlotManager parent;

    /**
     * 缓存的槽位物品和数据. 默认是
     */
    private final Map<PlayerSlot, ItemStack> itemCache = new ConcurrentHashMap<>(6);
    private final Map<PlayerSlot, Map<Class<?>, Object>> dataCache = new ConcurrentHashMap<>(6);

    /**
     * 异步修改专用槽. 一般不会有太多数据所以初始大小为1
     */
    private final Map<PlayerSlot, ItemStack> initialCache = new ConcurrentHashMap<>(1);
    private final Map<PlayerSlot, ItemStack> modifiedCache = new ConcurrentHashMap<>(1);

    private final Player player;

    public PlayerSlotCache(PlayerSlotManager parent, Player player) {
        this.parent = parent;
        this.player = player;
        for (PlayerSlot slot : parent.getRegisteredSlots()) {
            initSlot(slot);
        }
    }

    /**
     * 加载槽位到缓存中.
     * 初始化为全同步过程, 无视缓存
     *
     * @param slot 要加载的槽位
     */
    public void initSlot(PlayerSlot slot) {
        slot.get(player, item ->{
            item = item == null ? new ItemStack(Material.AIR) : item.clone();
            itemCache.put(slot, item);
            Map<Class<?>, Object> data = new ConcurrentHashMap<>();
            dataCache.put(slot, data);
            for (Map.Entry<Class<?>, Function<ItemStack, ?>> entry : parent.getDataReaders().entrySet()) {
                try {
                    Object info = entry.getValue().apply(item);
                    if (info != null) {
                        data.put(entry.getKey(), info);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 读取指定槽位数据
     *
     * @param slot  槽位
     * @param clazz 数据类
     */

    public void initData(PlayerSlot slot, Class<?> clazz) {
        Map<Class<?>, Object> data = dataCache.get(slot);
        if (data == null) {
            data = new ConcurrentHashMap<>();
            dataCache.put(slot, data);
        }
        Function<ItemStack, ?> func = parent.getDataReaders().get(clazz);
        if (func == null) {
            return;
        }
        data.put(clazz, func.apply(itemCache.get(slot)));
    }


    /**
     * 获取槽位中缓存的物品
     *
     * @param slot 要获取的槽位
     * @return 缓存物品
     */
    @NotNull
    public ItemStack getCachedItem(PlayerSlot slot) {
        return itemCache.getOrDefault(slot, AIR);
    }

    /**
     * 获取异步修改槽位中的物品
     * 如果是第一次获取, 顺便储存修改前的槽位状态
     * @param slot 槽位
     * @return 异步修改后的物品
     */
    @NotNull
    public ItemStack getModifiedItem(PlayerSlot slot){
        ItemStack modifiedItem = modifiedCache.get(slot);
        if(modifiedItem == null){
            modifiedItem = getCachedItem(slot);
            initialCache.put(slot, modifiedItem);
        }
        return modifiedItem;
    }

    /**
     * 对槽位进行异步修改，将结果缓存
     * 你必须要在这个方法调用前调用getModifiedItem
     *
     * @param slot 槽位
     * @param item 新的异步修改后的物品
     */
    public void setModifiedItem(PlayerSlot slot, ItemStack item){
        modifiedCache.put(slot, item);
    }

    /**
     * 将异步修改应用到玩家槽位中
     * 如果玩家槽位在异步计算期间已经改变
     * 那么修改不起作用 防止刷物品
     * 此方法必须同步调用
     *
     * @param ignoreDamage 是否忽略耐久变化
     * @return 修改的装备的数量。为0的话则没有修改任何装备
     */
    public void applyModification(boolean ignoreDamage){
        final List<Map.Entry<PlayerSlot, ItemStack>> slots = new ArrayList<>(modifiedCache.entrySet());
        modifiedCache.clear();
        initialCache.clear();
        for(Map.Entry<PlayerSlot, ItemStack> entry : slots) {
            PlayerSlot slot = entry.getKey();
            slot.get(player, truth ->{
                if (Util.isAir(truth)) {
                    // 禁止修改空气装备
                    return;
                }
                ItemStack verify = initialCache.get(slot);
                if (ignoreDamage){
                    if (verify.getType() != truth.getType() || verify.getAmount() != truth.getAmount() || !Bukkit.getItemFactory().equals(verify.getItemMeta(), truth.getItemMeta())) {
                        return;
                    }
                } else if(verify.equals(truth)){
                    return;
                }
                if(Bukkit.isPrimaryThread()||slot.isAsyncSafe()) {
                    slot.set(player, modifiedCache.getOrDefault(slot, AIR.clone()), result -> { });
                }else{
                    Bukkit.getScheduler().runTask(PlayerSlotAPI.getPlugin(),()->{
                        slot.set(player, modifiedCache.getOrDefault(slot, AIR.clone()), result -> { });
                    });
                }
            });
        }
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
    public <T> T getCachedData(PlayerSlot slot, Class<T> clazz) {
        return (T) dataCache.get(slot).get(clazz);
    }

    /**
     * 尝试更新槽位物品
     * 注意这个方法会被频繁调用
     *
     * @param slot 槽位类型
     * @param item 槽位物品
     */
    public void updateCachedItem(UpdateTrigger trigger, PlayerSlot slot, ItemStack item) {
        final ItemStack oldItem = itemCache.get(slot);
        final ItemStack newItem = item == null ? new ItemStack(Material.AIR) : item.clone();
        Bukkit.getScheduler().runTaskAsynchronously(PlayerSlotAPI.getPlugin(), () -> {
            if (oldItem.equals(newItem)) {
                return;
            }
            AsyncSlotUpdateEvent asyncEvent = new AsyncSlotUpdateEvent(trigger, player, slot, oldItem, newItem);
            Bukkit.getPluginManager().callEvent(asyncEvent);
            Queue<Consumer<AsyncSlotUpdateEvent>> updateHandlers = parent.getSlotUpdateHandlers(slot);
            if (updateHandlers != null) {
                for (Consumer<AsyncSlotUpdateEvent> updateHandler : updateHandlers) {
                    try {
                        updateHandler.accept(asyncEvent);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
            itemCache.put(slot, newItem);
            // 当信息读取器不为空时, 从装备上读取信息并丢入Map中
            Map<Class<?>, Object> data = dataCache.get(slot);
            if (data == null) {
                return;
            }
            for (Map.Entry<Class<?>, Function<ItemStack, ?>> entry : parent.getDataReaders().entrySet()) {
                try {
                    Object info = entry.getValue().apply(newItem);
                    if (info != null) {
                        data.put(entry.getKey(), info);
                    } else {
                        data.remove(entry.getKey());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 更新所有槽位
     */
    public void updateAll() {
        for (PlayerSlot slot : parent.getRegisteredSlots()) {
            slot.get(player, item-> updateCachedItem(UpdateTrigger.CUSTOM, slot, item));
        }
    }
}
