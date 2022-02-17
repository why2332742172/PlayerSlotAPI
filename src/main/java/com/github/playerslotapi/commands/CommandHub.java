package com.github.playerslotapi.commands;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.PlayerSlotPlugin;
import com.github.playerslotapi.slot.impl.DragonCoreSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class CommandHub implements CommandExecutor {

    //测试命令
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player senderPlayer = (Player) sender;
        if(args.length<3){
            senderPlayer.sendMessage("参数数量不正确");
            senderPlayer.sendMessage("psapi set/get player identity");
            return true;
        }
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if(targetPlayer == null){
            return true;
        }
        String identifier = args[2];
        DragonCoreSlot slot = new DragonCoreSlot(identifier);
        if(args[0].equalsIgnoreCase("set")){
            slot.set(targetPlayer,senderPlayer.getInventory().getItemInMainHand(),result->{
                senderPlayer.sendMessage(result? "设置成功":"设置失败");
            });
        }else if(args[0].equalsIgnoreCase("get")){
            slot.get(targetPlayer,a->{
                if (a == null) {
                    senderPlayer.sendMessage("获取失败");
                } else if(a.getType() == Material.AIR){
                    senderPlayer.sendMessage("该槽位物品为空气");
                }else {
                    senderPlayer.sendMessage(a.toString());
                    if(Bukkit.isPrimaryThread()){
                        senderPlayer.getInventory().addItem(a);
                    }else{
                        Bukkit.getScheduler().runTask(PlayerSlotPlugin.getInstance(),()->{
                            senderPlayer.getInventory().addItem(a);
                        });
                    }
                }
            });
        }
        return true;
    }
}
