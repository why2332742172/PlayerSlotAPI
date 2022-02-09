package com.github.playerslotapi;

import com.github.playerslotapi.core.PlayerCache;
import com.github.playerslotapi.core.PlayerCacheManager;
import com.github.playerslotapi.hook.DragonCoreHook;
import com.github.playerslotapi.hook.GermPluginHook;
import com.github.playerslotapi.slot.AbstractSlot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

public class PlayerSlotAPI {

    /**
     * 核心管理器
     */
    private static final PlayerCacheManager MANAGER;
    // 依赖
    public static GermPluginHook germPluginHook = null;
    public static DragonCoreHook dragonCoreHook = null;
    private static Plugin PLUGIN = null;

    static {
        MANAGER = new PlayerCacheManager();
    }


    private PlayerSlotAPI() {
    }

    public static Plugin getPlugin() {
        return PLUGIN;
    }

    /**
     * 槽位注册
     *
     * @param slot 要注册的槽位类型
     */

    public static void registerSlot(AbstractSlot slot) {
        PlayerCache.registerSlot(slot);
    }

    /**
     * 注册原版槽位和监听事件
     */
    public static void registerVanilla() {
        PlayerCache.registerVanilla();
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
        PlayerCache.registerDataReader(clazz, reader);
    }

    /**
     * 获取玩家槽位缓存信息
     *
     * @param player 要获取的玩家
     * @return 玩家槽位缓存
     */
    public static PlayerCache getCache(Player player) {
        return MANAGER.getPlayerCache(player);
    }

    /**
     * 重载所有缓存
     */

    public static void reload() {
        MANAGER.reload();
    }

    /**
     * 初始化
     *
     * @param plugin 所属插件主类
     */
    public static void init(Plugin plugin) {
        PLUGIN = plugin;
        if (Bukkit.getPluginManager().getPlugin("GermPlugin") != null) {
            //萌芽
            germPluginHook = new GermPluginHook();
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已加载GermPlugin作为前置!");
        } else if (Bukkit.getPluginManager().getPlugin("DragonCore") != null) {
            //龙核
            dragonCoreHook = new DragonCoreHook();
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已加载DragonCore作为前置!");
        }
        MANAGER.registerEvents();
    }
}
