package com.github.playerslotapi;

import com.github.playerslotapi.commands.CommandHub;
import com.github.playerslotapi.core.PlayerCache;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerSlotPlugin extends JavaPlugin {

    private static PlayerSlotPlugin instance;

    public static PlayerSlotPlugin getInstance() {
        return instance;
    }


    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        printInfo();
        PlayerSlotAPI.init(this);
        PlayerSlotAPI.registerVanilla();
        //测试命令
        PluginCommand command = getCommand("psapi");
        if (command != null) {
            command.setExecutor(new CommandHub());
        }
        this.saveDefaultConfig();
        this.reloadConfig();
        Events.subscribe(AsyncSlotUpdateEvent.class,event->{
            event.getPlayer().sendMessage("装备更新 - " + event.getSlot().toString() + ": "
            +event.getOldItem().getType().toString()+" -> " + event.getNewItem().getType().toString());
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9Disabling PlayerSlotApi");
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
    }

    private void printInfo() {
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9Enabling PlayerSlotApi");
        Bukkit.getConsoleSender().sendMessage("§9==============================================================");
        Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f Loading...");
    }
}
