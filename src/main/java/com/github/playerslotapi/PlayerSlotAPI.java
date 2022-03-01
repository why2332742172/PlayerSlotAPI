package com.github.playerslotapi;

import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.hook.DragonCoreHook;
import com.github.playerslotapi.hook.GermPluginHook;
import com.github.playerslotapi.hook.VanillaHook;
import com.github.playerslotapi.slot.PlayerSlot;
import com.github.playerslotapi.slot.PlayerSlotCache;
import com.github.playerslotapi.slot.impl.DragonCoreSlot;
import com.github.playerslotapi.slot.impl.GermPluginSlot;
import com.github.playerslotapi.slot.impl.VanillaEquipSlot;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家槽位API工具
 * 静态单例, 不可实例化
 * 建议自行relocate
 */
public class PlayerSlotAPI {

    private static final GermPluginHook GERM_PLUGIN_HOOK;
    private static final DragonCoreHook DRAGON_CORE_HOOK;
    private static String PREFIX = "§9[§ePlayerSlotAPI§9]§f";

    /**
     * 核心管理器实例
     */
    private static final PlayerSlotAPI API;
    /**
     * 加载本API的插件
     */
    private static final Plugin PLUGIN;

    static {
        if (Bukkit.getPluginManager().getPlugin("GermPlugin") != null) {
            //萌芽
            GERM_PLUGIN_HOOK = new GermPluginHook();
            DragonCoreHook.register();
            Bukkit.getConsoleSender().sendMessage(PREFIX+"已加载GermPlugin作为前置!");
        } else {
            GERM_PLUGIN_HOOK = null;
        }
        if (Bukkit.getPluginManager().getPlugin("DragonCore") != null) {
            //龙核
            DRAGON_CORE_HOOK = new DragonCoreHook();
            GermPluginHook.register();
            Bukkit.getConsoleSender().sendMessage(PREFIX+"已加载DragonCore作为前置!");
        } else {
            DRAGON_CORE_HOOK = null;
        }
        ClassLoader loader = PlayerSlotAPI.class.getClassLoader();
        try {
            Class<?> pluginClassLoader = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
            while (!(pluginClassLoader.isInstance(loader))) {
                loader = loader.getParent();
                if (loader == null) {
                    throw new RuntimeException(PREFIX + "错误：未找到Bukkit插件主类");
                }
            }
            Field field = pluginClassLoader.getDeclaredField("plugin");
            field.setAccessible(true);
            PLUGIN = (Plugin) field.get(loader);
            API = new PlayerSlotAPI();
        }catch (Exception e){
            throw new RuntimeException(PREFIX + "错误：未找到Bukkit插件主类");
        }
    }

    private final Map<String, PlayerSlot> SLOT_MAP = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerSlotCache> PLAYER_MAP = new ConcurrentHashMap<>();

    private PlayerSlotAPI() {
        Events.subscribe(PlayerJoinEvent.class, event -> {
            PLAYER_MAP.put(event.getPlayer().getUniqueId(), new PlayerSlotCache(event.getPlayer()));
        });
        Events.subscribe(PlayerQuitEvent.class, event -> {
            PLAYER_MAP.remove(event.getPlayer().getUniqueId());
        });
        Events.subscribe(PlayerKickEvent.class, event -> {
            PLAYER_MAP.remove(event.getPlayer().getUniqueId());
        });
        Events.subscribe(SlotUpdateEvent.class, this::onSlotUpdate);
        Events.subscribe(PlayerTeleportEvent.class, this::onWorldChange);
        Events.subscribe(PlayerRespawnEvent.class, this::onPlayerRespawn);
        reload();
    }

    public static GermPluginHook getGermPluginHook() {
        return GERM_PLUGIN_HOOK;
    }

    public static DragonCoreHook getDragonCoreHook() {
        return DRAGON_CORE_HOOK;
    }

    public static Plugin getPlugin() {
        return PLUGIN;
    }

    public static PlayerSlotAPI getAPI() {
        return API;
    }


    /**
     * 获取所有已经注册的槽位
     *
     * @return 已经注册的槽位
     */

    public Map<String, PlayerSlot> getSlotMap() {
        return SLOT_MAP;
    }

    /**
     * 槽位注册
     *
     * @param slot 要注册的槽位类型
     */
    public void registerSlot(PlayerSlot slot) {
        if (SLOT_MAP.containsKey(slot.toString())) {
            return;
        }
        if (slot instanceof DragonCoreSlot) {
            if (PlayerSlotAPI.DRAGON_CORE_HOOK == null) {
                return;
            }
            SLOT_MAP.put(((DragonCoreSlot) slot).getIdentifier(), slot);
        } else if (slot instanceof GermPluginSlot) {
            if (PlayerSlotAPI.GERM_PLUGIN_HOOK == null) {
                return;
            }
            SLOT_MAP.put(((GermPluginSlot) slot).getIdentifier(), slot);
        } else {
            SLOT_MAP.put(slot.toString(), slot);
        }
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
        VanillaHook.registerEvents();
    }

    /**
     * 重载槽位缓存
     */
    public void reload() {
        PLAYER_MAP.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PLAYER_MAP.put(player.getUniqueId(), new PlayerSlotCache(player));
        }
    }

    public PlayerSlotCache getSlotCache(Player player) {
        PlayerSlotCache result = PLAYER_MAP.get(player.getUniqueId());
        if (result == null) {
            result = new PlayerSlotCache(player);
            PLAYER_MAP.put(player.getUniqueId(), result);
        }
        return result;
    }


    private void onSlotUpdate(SlotUpdateEvent event) {
        PlayerSlotCache cache = getSlotCache(event.getPlayer());
        if (event.isUpdateImmediately()) {
            // 如果是forceUpdate, 立即更新无视准确性
            cache.updateItem(event.getTrigger(), event.getSlot(), event.getNewItem());
        } else {
            // 否则延迟1 tick 检查, 准确更新装备缓存
            Bukkit.getScheduler().runTask(PlayerSlotAPI.getPlugin(), () -> {
                event.getSlot().get(event.getPlayer(),
                        item -> cache.updateItem(event.getTrigger(), event.getSlot(), item));
            });
        }
    }

    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(PlayerSlotAPI.getPlugin(), () -> getSlotCache(event.getPlayer()).updateAll(), 1L);
    }

    private void onWorldChange(PlayerTeleportEvent event) {
        Bukkit.getScheduler().runTaskLater(PlayerSlotAPI.getPlugin(), () -> getSlotCache(event.getPlayer()).updateAll(), 1L);
    }
}
