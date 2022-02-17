package com.github.playerslotapi.slot.impl;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.slot.PlayerSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GermPluginSlot extends PlayerSlot {

    private final String identifier;

    public GermPluginSlot(String identifier) {
        super("GERM_PLUGIN_" + identifier, "GERM_PLUGIN_SLOT");
        this.identifier = identifier;
    }

    @Override
    public boolean isAsyncSafe() {
        return true;
    }

    @Override
    public void get(Player player, Consumer<ItemStack> callback) {
        callback.accept(PlayerSlotAPI.germPluginHook.getItemFromSlot(player, identifier));
    }

    @Override
    public void set(Player player, ItemStack item, Consumer<Boolean> callback) {
        PlayerSlotAPI.germPluginHook.setItemToSlot(player, identifier, item);
        callback.accept(true);
    }
}
