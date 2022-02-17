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

    public ItemStack getItemFromSlot(Player player, String identity) {
        return GermSlotAPI.getItemStackFromIdentity(player, identity);
    }

    public void setItemToSlot(Player player, String identifier, ItemStack toBePuttedItem) {
        GermSlotAPI.saveItemStackToIdentity(player, identifier, toBePuttedItem);
    }
}
