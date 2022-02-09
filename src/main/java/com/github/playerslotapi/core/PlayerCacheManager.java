package com.github.playerslotapi.core;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.event.CheckEquipEvent;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCacheManager {

    private static final Map<UUID, PlayerCache> PLAYER_MAP = new ConcurrentHashMap<>();

    public static void registerEvents() {
        Events.subscribe(PlayerJoinEvent.class, event -> {
            PLAYER_MAP.put(event.getPlayer().getUniqueId(), new PlayerCache(event.getPlayer()));
        });
        Events.subscribe(PlayerQuitEvent.class, event -> {
            PLAYER_MAP.remove(event.getPlayer().getUniqueId());
        });
        Events.subscribe(PlayerKickEvent.class, event -> {
            PLAYER_MAP.remove(event.getPlayer().getUniqueId());
        });
        Events.subscribe(CheckEquipEvent.class, PlayerCacheManager::onItemEquip);
        Events.subscribe(PlayerTeleportEvent.class, PlayerCacheManager::onWorldChange);
        Events.subscribe(PlayerRespawnEvent.class, PlayerCacheManager::onPlayerRespawn);
    }

    public static void reload() {
        PLAYER_MAP.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PLAYER_MAP.put(player.getUniqueId(), new PlayerCache(player));
        }
    }

    public static PlayerCache getPlayerCache(Player player) {
        PlayerCache result = PLAYER_MAP.get(player.getUniqueId());
        if (result == null) {
            result = new PlayerCache(player);
            PLAYER_MAP.put(player.getUniqueId(), result);
        }
        return result;
    }

    // 延迟1 tick 检查, 先发动技能再更新装备
    private static void onItemEquip(CheckEquipEvent event) {
        Bukkit.getScheduler().runTaskLater(PlayerSlotAPI.getPlugin(), () -> {
            PlayerCache cache = getPlayerCache(event.getPlayer());
            cache.updateCachedItem(event.getSlot(), event.getSlot().get(event.getPlayer()));
        }, 1L);
    }

    private static void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(PlayerSlotAPI.getPlugin(), () -> getPlayerCache(event.getPlayer()).updateAll(), 1L);
    }

    private static void onWorldChange(PlayerTeleportEvent event) {
        Bukkit.getScheduler().runTaskLater(PlayerSlotAPI.getPlugin(), () -> getPlayerCache(event.getPlayer()).updateAll(), 1L);
    }
}
