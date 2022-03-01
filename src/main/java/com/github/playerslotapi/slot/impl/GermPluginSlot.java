package com.github.playerslotapi.slot.impl;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.event.UpdateTrigger;
import com.github.playerslotapi.slot.PlayerSlot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GermPluginSlot extends PlayerSlot {

    private static boolean disableCacheUpdate = false;
    private final String identifier;

    public GermPluginSlot(String identifier) {
        super("GERM_PLUGIN_" + identifier, "GERM_PLUGIN_SLOT");
        this.identifier = identifier;
    }

    public static void disableCacheUpdate() {
        disableCacheUpdate = true;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isAsyncSafe() {
        return true;
    }

    @Override
    public void get(Player player, Consumer<ItemStack> callback) {
        callback.accept(PlayerSlotAPI.getGermPluginHook().getItemFromSlot(player, identifier));
    }

    @Override
    public void set(Player player, ItemStack item, Consumer<Boolean> callback) {
        PlayerSlotAPI.getGermPluginHook().setItemToSlot(player, identifier, item);
        if (!disableCacheUpdate) {
            SlotUpdateEvent updateEvent = new SlotUpdateEvent(UpdateTrigger.SET, player, this, null, item);
            updateEvent.setUpdateImmediately();
            Bukkit.getPluginManager().callEvent(updateEvent);
        }
        callback.accept(true);
    }
}
