package com.github.playerslotapi;

import com.github.playerslotapi.core.PlayerSlotManager;
import com.github.playerslotapi.hook.DragonCoreHook;
import com.github.playerslotapi.hook.GermPluginHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PlayerSlotAPI {

    // 依赖
    public static GermPluginHook germPluginHook = null;
    public static DragonCoreHook dragonCoreHook = null;
    public static Plugin PLUGIN = null;
    /**
     * 核心管理器
     */
    private static PlayerSlotManager PUBLIC_MANAGER;

    static {
        PUBLIC_MANAGER = null;
    }


    private PlayerSlotAPI() {
    }

    public static Plugin getPlugin() {
        return PLUGIN;
    }

    public static PlayerSlotManager getPublicManager() {
        if (PUBLIC_MANAGER == null) {
            PUBLIC_MANAGER = new PlayerSlotManager();
            PUBLIC_MANAGER.registerVanilla();
            PUBLIC_MANAGER.registerEvents();
        }
        return PUBLIC_MANAGER;
    }

    public static PlayerSlotManager getPrivateManger() {
        PlayerSlotManager privateManager = new PlayerSlotManager();
        privateManager.registerEvents();
        return privateManager;
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
        }
        if (Bukkit.getPluginManager().getPlugin("DragonCore") != null) {
            //龙核
            dragonCoreHook = new DragonCoreHook();
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已加载DragonCore作为前置!");
        }
    }
}
