package com.github.playerslotapi.core;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.listener.VanillaListener;
import com.github.playerslotapi.slot.PlayerSlot;
import com.github.playerslotapi.slot.impl.VanillaEquipSlot;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlayerSlotManager {

    private final Set<PlayerSlot> REGISTERED_SLOTS = new HashSet<>();
    private final Map<Class<?>, Function<ItemStack, ?>> DATA_READER = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerSlotCache> PLAYER_MAP = new ConcurrentHashMap<>();
    private final Map<PlayerSlot, Queue<Consumer<AsyncSlotUpdateEvent>>> SLOT_UPDATE_HANDLERS = new ConcurrentHashMap<>();


    /**
     * 获取所有已经注册的槽位
     *
     * @return 已经注册的槽位
     */
    public Set<PlayerSlot> getRegisteredSlots() {
        return REGISTERED_SLOTS;
    }

    /**
     * 获取所有已经注册的装备信息读取器
     */
    public Map<Class<?>, Function<ItemStack, ?>> getDataReaders() {
        return DATA_READER;
    }

    /**
     * 获取订阅了指定槽位的装备更新器
     *
     * @param slot 槽位
     * @return 装备更新器
     */

    public Queue<Consumer<AsyncSlotUpdateEvent>> getSlotUpdateHandlers(PlayerSlot slot) {
        return SLOT_UPDATE_HANDLERS.get(slot);
    }


    /**
     * 订阅槽位异步更新
     *
     * @param slot     要订阅的槽位
     * @param consumer 事件处理函数
     */
    public void subscribe(PlayerSlot slot, Consumer<AsyncSlotUpdateEvent> consumer) {
        Queue<Consumer<AsyncSlotUpdateEvent>> consumers = SLOT_UPDATE_HANDLERS.get(slot);
        if (consumers == null) {
            consumers = new ConcurrentLinkedQueue<>();
            SLOT_UPDATE_HANDLERS.putIfAbsent(slot, consumers);
        }
        consumers.add(consumer);
    }


    /**
     * 槽位注册
     *
     * @param slot 要注册的槽位类型
     */
    public void registerSlot(PlayerSlot slot) {
        REGISTERED_SLOTS.add(slot);
        for (PlayerSlotCache cache : PLAYER_MAP.values()) {
            cache.initSlot(slot);
        }
    }

    /**
     * 注册原版槽位
     */
    public void registerVanilla() {
        registerSlot(VanillaEquipSlot.MAINHAND);
        registerSlot(VanillaEquipSlot.OFFHAND);
        registerSlot(VanillaEquipSlot.HELMET);
        registerSlot(VanillaEquipSlot.CHESTPLATE);
        registerSlot(VanillaEquipSlot.LEGGINGS);
        registerSlot(VanillaEquipSlot.BOOTS);
        VanillaListener.registerEvents();
    }

    /**
     * 注册装备信息读取器
     * 读取器负责从装备上读取出指定的信息
     * 装备更新时, 读取器将被异步调用
     *
     * @param clazz  信息类型
     * @param reader 读取器
     */
    public <T> void registerDataReader(Class<T> clazz, Function<ItemStack, T> reader) {
        DATA_READER.put(clazz, reader);
        for (PlayerSlotCache cache : PLAYER_MAP.values()) {
            for (PlayerSlot slot : REGISTERED_SLOTS) {
                cache.initData(slot, clazz);
            }
        }
    }

    /**
     * 初始化时注册事件
     */
    public void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class, event -> {
            PLAYER_MAP.put(event.getPlayer().getUniqueId(), new PlayerSlotCache(this, event.getPlayer()));
        });
        Events.subscribe(PlayerQuitEvent.class, event -> {
            PLAYER_MAP.remove(event.getPlayer().getUniqueId());
        });
        Events.subscribe(PlayerKickEvent.class, event -> {
            PLAYER_MAP.remove(event.getPlayer().getUniqueId());
        });
        Events.subscribe(SlotUpdateEvent.class, this::onItemEquip);
        Events.subscribe(PlayerTeleportEvent.class, this::onWorldChange);
        Events.subscribe(PlayerRespawnEvent.class, this::onPlayerRespawn);
    }

    public void reload() {
        PLAYER_MAP.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PLAYER_MAP.put(player.getUniqueId(), new PlayerSlotCache(this, player));
        }
    }

    public PlayerSlotCache getPlayerCache(Player player) {
        PlayerSlotCache result = PLAYER_MAP.get(player.getUniqueId());
        if (result == null) {
            result = new PlayerSlotCache(this, player);
            PLAYER_MAP.put(player.getUniqueId(), result);
        }
        return result;
    }

    // 延迟1 tick 检查, 先发动技能再更新装备
    private void onItemEquip(SlotUpdateEvent event) {
        Bukkit.getScheduler().runTask(PlayerSlotAPI.getPlugin(), () -> {
            PlayerSlotCache cache = getPlayerCache(event.getPlayer());
            cache.updateCachedItem(event.getTrigger(),event.getSlot(), event.getSlot().get(event.getPlayer()));
        });
    }

    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(PlayerSlotAPI.getPlugin(), () -> getPlayerCache(event.getPlayer()).updateAll(), 1L);
    }

    private void onWorldChange(PlayerTeleportEvent event) {
        Bukkit.getScheduler().runTaskLater(PlayerSlotAPI.getPlugin(), () -> getPlayerCache(event.getPlayer()).updateAll(), 1L);
    }
}
