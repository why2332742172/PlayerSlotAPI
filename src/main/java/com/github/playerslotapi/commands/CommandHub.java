package com.github.playerslotapi.commands;

import com.github.playerslotapi.PlayerSlotAPI;
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
        if (args.length == 2) {
            //psapi 玩家id 槽位identifier
            //输出该槽位的物品信息
            ItemStack a = PlayerSlotAPI.dragonCoreHook.getItemFromSlot(args[1], Bukkit.getPlayer(args[0]));
            if (a == null || a.getType() == Material.AIR) {
                senderPlayer.sendMessage("null");
            } else {
                senderPlayer.sendMessage(a.toString());
            }

            return true;
        } else if (args.length == 3) {
            //psapi 玩家id identifier true/false(是否为强制?)
            //给该玩家的槽位设置物品
            PlayerSlotAPI.dragonCoreHook.setItemToSlot(args[1], Bukkit.getPlayer(args[0]), senderPlayer.getItemInHand());
            return true;
        }

        return false;
    }
}
