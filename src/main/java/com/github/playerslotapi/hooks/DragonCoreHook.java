package com.github.playerslotapi.hooks;


import eos.moe.dragoncore.DragonCore;
import eos.moe.dragoncore.database.IDataBase;
import eos.moe.dragoncore.network.PacketSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DragonCoreHook {

    private final DragonCore instance;

    public DragonCoreHook() {
        this.instance = DragonCore.getInstance();
    }

    public ItemStack getItemFromSlot(String identifier, Player player) {
        final ItemStack[] result = {null};
        instance.getDB().getData(player, identifier, new IDataBase.Callback<ItemStack>() {
            @Override
            public void onResult(ItemStack itemStack) {
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    //有物品则返回该物品
                    result[0] = itemStack;
                }
            }

            @Override
            public void onFail() {
                Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 获取玩家:" + player + "的" + identifier + "槽位物品时出错");
            }
        });

        return result[0];
    }

    public void setItemToSlot(String identifier, Player player, ItemStack toBePuttedItem, Boolean forceReplace) {
        ItemStack itemInSlot = getItemFromSlot(identifier, player);

        if (itemInSlot != null && itemInSlot.getType() != Material.AIR && !forceReplace) {
            player.sendMessage("玩家" + player.getName() + "在槽位" + identifier + "已经有物品了!无法非强制替换!");
            return;
        }
        instance.getDB().setData(player, identifier, toBePuttedItem, new IDataBase.Callback<ItemStack>() {
            @Override
            public void onResult(ItemStack itemStack) {
                if (toBePuttedItem != null) {
                    PacketSender.putClientSlotItem(player, identifier, toBePuttedItem);
                } else {
                    //为null代表删除该槽位物品
                    PacketSender.putClientSlotItem(player, identifier, new ItemStack(Material.AIR));
                }
            }

            @Override
            public void onFail() {
                Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotApi§9]§f 为玩家:" + player + "的" + identifier + "槽位设置物品时出错");
            }
        });
    }


}
