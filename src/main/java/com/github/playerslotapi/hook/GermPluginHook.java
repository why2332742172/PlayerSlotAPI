package com.github.playerslotapi.hook;

import com.germ.germplugin.GermPlugin;
import com.germ.germplugin.api.GermSlotAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GermPluginHook {

    public GermPlugin instance;

    public GermPluginHook() {
        this.instance = GermPlugin.getPlugin();
    }

    public ItemStack getItemFromSlot(String identifier, Player player) {
        List<String> list = new ArrayList<>();
        list.add(identifier);
        return GermSlotAPI.getGermSlotItemStacks(player, list).get(0);
    }

    public void setItemToSlot(String identifier, Player player, ItemStack toBePuttedItem) {
        GermSlotAPI.saveItemStackToIdentity(player, identifier, toBePuttedItem);
    }
}
