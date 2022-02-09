package com.github.playerslotapi;

import com.github.playerslotapi.commands.CommandHub;
import com.github.playerslotapi.hooks.DragonCoreHook;
import com.github.playerslotapi.hooks.GermPluginHook;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerSlotApi extends JavaPlugin {

    //依赖
    public static GermPluginHook germPluginHook = null;
    public static DragonCoreHook dragonCoreHook = null;

    //本体
    private static PlayerSlotApi instance;

    public static PlayerSlotApi getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        printInfo();
        loadHook();
        //测试命令
        PluginCommand command = getCommand("psapi");
        if(command != null){
            command.setExecutor(new CommandHub());
        }
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9Disabling PlayerSlotApi");
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
    }

    private void printInfo(){
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9Enabling PlayerSlotApi");
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f Loading...");
    }

    private void loadHook(){
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
