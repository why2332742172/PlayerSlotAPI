package com.github.playerslotapi;

import com.github.playerslotapi.commands.CommandHub;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerSlotPlugin extends JavaPlugin {

    private static PlayerSlotPlugin instance;
    private static PlayerSlotAPI slotApi;

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
        slotApi = PlayerSlotAPI.getAPI();
        slotApi.registerVanilla();
        slotApi.reload();
        //测试命令
        PluginCommand command = getCommand("psapi");
        if (command != null) {
            command.setExecutor(new CommandHub());
        }
        Events.subscribe(AsyncSlotUpdateEvent.class, event -> {
            event.getPlayer().sendMessage("异步装备更新 - "
                    + event.getSlot().toString() + ": "
                    + event.getOldItem().getType().toString() + "*" + event.getOldItem().getAmount()
                    + " -> "
                    + event.getNewItem().getType().toString() + "*" + event.getNewItem().getAmount());
        });
        Events.subscribe(SlotUpdateEvent.class, event -> {
            event.getPlayer().sendMessage("同步装备更新 - " + event.getSlot().toString() + ": "
                    + event.getOldItem().getType().toString() + "*" + event.getOldItem().getAmount() +
                    " -> " + (event.getNewItem() == null ? "无法判断" :
                    event.getNewItem().getType().toString() + "*" + event.getNewItem().getAmount()));
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
        Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotAPI§9]§fLoading...");
    }
}
