package com.github.playerslotapi.event;

import com.github.playerslotapi.slot.AbstractSlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class AsyncSlotUpdateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final AbstractSlot slot;
    private final ItemStack oldItem;
    private boolean isCancelled;

    private ItemStack newItem;

    public AsyncSlotUpdateEvent(Player player, AbstractSlot slot, ItemStack oldItem, ItemStack newItem) {
        super(true);
        this.player = player;
        this.slot = slot;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public AbstractSlot getSlot() {
        return slot;
    }

    public ItemStack getOldItem() {
        return oldItem;
    }

    public ItemStack getNewItem() {
        if (newItem == null || newItem.getType() == Material.AIR) {
            newItem = new ItemStack(Material.AIR);
        }
        return newItem;
    }

    public void setNewItem(ItemStack newItem) {
        this.newItem = newItem;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
