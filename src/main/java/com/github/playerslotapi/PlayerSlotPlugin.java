package com.github.playerslotapi;

import com.github.playerslotapi.commands.CommandHub;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
        PlayerSlotAPI.registerDataReader(TestInfo.class,item->{
            ItemMeta meta = item.getItemMeta();
            if(meta != null){
                List<String> lores = meta.getLore();
                if(lores != null){
                    for(String lore : lores){
                        int index = lore.indexOf("装备信息:");
                        if(index!=-1){
                            return new TestInfo(lore.substring(index+5));
                        }
                    }
                }
            }
            return null;
        });
        PlayerSlotAPI.reload();
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
            Bukkit.getScheduler().runTaskLaterAsynchronously(this,()->{
                TestInfo info = PlayerSlotAPI.getCache(event.getPlayer()).getCachedData(event.getSlot(),TestInfo.class);
                if(info!=null){
                    event.getPlayer().sendMessage(info.getInfo());
                }
            },1L);
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
