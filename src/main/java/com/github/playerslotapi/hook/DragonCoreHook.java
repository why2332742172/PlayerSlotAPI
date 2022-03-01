package com.github.playerslotapi.hook;


import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.event.UpdateTrigger;
import com.github.playerslotapi.slot.PlayerSlot;
import com.github.playerslotapi.util.Events;
import com.github.playerslotapi.util.Util;
import eos.moe.dragoncore.DragonCore;
import eos.moe.dragoncore.api.event.PlayerSlotUpdateEvent;
import eos.moe.dragoncore.database.IDataBase;
import eos.moe.dragoncore.network.PacketSender;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class DragonCoreHook {

    private final DragonCore instance;

    public DragonCoreHook() {
        this.instance = DragonCore.getInstance();
    }

    public static void register() {
        Events.subscribe(PlayerSlotUpdateEvent.class, event -> {
            ItemStack newItem = event.getItemStack();
            String identifier = event.getIdentifier();
            //龙核特有傻逼 进服时identifier是null
            if (identifier != null) {
                if (PlayerSlotAPI.getAPI().getSlotMap().containsKey(identifier)) {
                    PlayerSlot slot = PlayerSlotAPI.getAPI().getSlotMap().get(identifier);
                    SlotUpdateEvent update = new SlotUpdateEvent(UpdateTrigger.DRAGON_CORE, event.getPlayer(), slot, null, newItem);
                    update.setUpdateImmediately();
                    Bukkit.getPluginManager().callEvent(update);
                }
            }
        });
    }

    public void getItemFromSlot(Player player, String identifier, Consumer<ItemStack> callback) {
        instance.getDB().getData(player, identifier, new IDataBase.Callback<ItemStack>() {
            @Override
            public void onResult(ItemStack itemStack) {
                if (!Util.isAir(itemStack)) {
                    //有物品则返回该物品
                    callback.accept(itemStack);
                } else {
                    callback.accept(new ItemStack(Material.AIR));
                }
            }

            @Override
            public void onFail() {
                callback.accept(null);
            }
        });
    }

    public void setItemToSlot(Player player, String identifier, ItemStack toBePuttedItem, Consumer<Boolean> callback) {
        instance.getDB().setData(player, identifier, toBePuttedItem, new IDataBase.Callback<ItemStack>() {
            @Override
            public void onResult(ItemStack itemStack) {
                if (toBePuttedItem != null) {
                    PacketSender.putClientSlotItem(player, identifier, toBePuttedItem);
                } else {
                    //为null代表删除该槽位物品
                    PacketSender.putClientSlotItem(player, identifier, new ItemStack(Material.AIR));
                }
                callback.accept(true);
            }

            @Override
            public void onFail() {
                Bukkit.getConsoleSender().sendMessage("§9[§ePlayerSlotAPI§9]§f 为玩家:" + player + "的" + identifier + "槽位设置物品时出错");
                callback.accept(false);
            }
        });
    }


}
