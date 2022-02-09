package com.github.playerslotapi;

import com.github.playerslotapi.hooks.DragonCoreHook;
import com.github.playerslotapi.hooks.GermPluginHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PlayerSlotAPI {

    private static final PlayerSlotAPI INSTANCE;
    private static Plugin PLUGIN = null;

    static {
        INSTANCE = new PlayerSlotAPI();
    }

    public static PlayerSlotAPI getApi(){
        return INSTANCE;
    }

    public static Plugin getPlugin(){
        return PLUGIN;
    }

    private PlayerSlotAPI(){}

    //依赖
    public static GermPluginHook germPluginHook = null;
    public static DragonCoreHook dragonCoreHook = null;

    public static void init(Plugin plugin){
        PLUGIN = plugin;
        if (Bukkit.getPluginManager().getPlugin("GermPlugin") != null){
            //萌芽
            germPluginHook = new GermPluginHook();
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已加载GermPlugin作为前置!");
        }else if (Bukkit.getPluginManager().getPlugin("DragonCore") != null){
            //龙核
            dragonCoreHook = new DragonCoreHook();
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已加载DragonCore作为前置!");
        }else{
            //原版
            Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 已选择原版模式!");
        }
    }
}
