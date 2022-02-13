package com.github.playerslotapi.slot.impl;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.slot.PlayerSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GermPluginSlot extends PlayerSlot {

    private final String identifier;

    public GermPluginSlot(String identifier) {
        super("GERM_PLUGIN_" + identifier, "GERM_PLUGIN_SLOT");
        this.identifier = identifier;
    }

    @Override
    public ItemStack get(Player player) {
        return PlayerSlotAPI.germPluginHook.getItemFromSlot(identifier, player);
    }

    @Override
    public void set(Player player, ItemStack item) {
        PlayerSlotAPI.germPluginHook.setItemToSlot(identifier, player, item);
    }
}
