package com.github.playerslotapi.slot.impl;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.slot.AbstractSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DragonCoreSlot extends AbstractSlot {

    private final String identifier;

    public DragonCoreSlot(String identifier) {
        super("DRAGON_CORE_" + identifier, "DRAGON_CORE_SLOT");
        this.identifier = identifier;
    }

    @Override
    public ItemStack get(Player player) {
        return PlayerSlotAPI.dragonCoreHook.getItemFromSlot(identifier, player);
    }

    @Override
    public void set(Player player, ItemStack item) {
        PlayerSlotAPI.dragonCoreHook.setItemToSlot(identifier, player, item);
    }
}
