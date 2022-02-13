package com.github.playerslotapi;

import com.github.playerslotapi.commands.CommandHub;
import com.github.playerslotapi.core.PlayerSlotManager;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlayerSlotPlugin extends JavaPlugin {

    private static PlayerSlotPlugin instance;
    private static PlayerSlotManager manager;

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
        manager = PlayerSlotAPI.getPublicManager();
        manager.registerDataReader(TestInfo.class, item -> {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lores = meta.getLore();
                if (lores != null) {
                    for (String lore : lores) {
                        int index = lore.indexOf("装备信息:");
                        if (index != -1) {
                            return new TestInfo(lore.substring(index + 5));
                        }
                    }
                }
            }
            return null;
        });
        manager.reload();
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
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                TestInfo info = manager.getPlayerCache(event.getPlayer()).getCachedData(event.getSlot(), TestInfo.class);
                if (info != null) {
                    event.getPlayer().sendMessage(info.getInfo());
                }
            }, 1L);
        });
        Events.subscribe(SlotUpdateEvent.class, event->{
            event.getPlayer().sendMessage("同步装备更新 - " + event.getSlot().toString() + ": "
                    + event.getOldItem().getType().toString() + "*" + event.getOldItem().getAmount()+
                    " -> " + (event.getNewItem() == null? "无法判断":
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
        Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f Loading...");
    }
}
