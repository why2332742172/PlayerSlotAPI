package com.github.playerslotapi.hooks;

import com.germ.germplugin.api.GermKeyAPI;
import com.germ.germplugin.api.GermSlotAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GermPluginHook {

    public GermKeyAPI germKeyAPI = null;

    public GermPluginHook(){
        this.germKeyAPI = new GermKeyAPI();
    }


    public ItemStack getItemFromSlot(String identifier, Player player) {
        List<String> list = new ArrayList<>();
        list.add(identifier);
        return GermSlotAPI.getGermSlotItemStacks(player, list).get(0);
    }


    public void setItemToSlot(String identifier, Player player, ItemStack toBePuttedItem, Boolean forceReplace) {
        ItemStack itemInSlot = getItemFromSlot(identifier, player);
        player.sendMessage(forceReplace.toString());
        if (itemInSlot != null && itemInSlot.getType() != Material.AIR && !forceReplace) {
            player.sendMessage("玩家" + player.getName() + "在槽位" + identifier + "已经有物品了!无法非强制替换!");
            return;
        }
        GermSlotAPI.saveItemStackToIdentity(player, identifier, toBePuttedItem);
    }


}
