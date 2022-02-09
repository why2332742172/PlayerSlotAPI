package com.github.playerslotapi.event;

import com.github.playerslotapi.slot.AbstractSlot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class CheckEquipEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final AbstractSlot slot;
    private final CheckTrigger trigger;

    public CheckEquipEvent(final Player player, final CheckTrigger trigger, final AbstractSlot slot) {
        super(player);
        this.trigger = trigger;
        this.slot = slot;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public final HandlerList getHandlers() {
        return HANDLERS;
    }


    public final AbstractSlot getSlot() {
        return slot;
    }

    public final CheckTrigger getTrigger() {
        return trigger;
    }

    public enum CheckTrigger {
        /**
         * When admin use command to edit equip
         */
        COMMAND,

        /**
         * When you use /hat to put on a hat
         */
        COMMAND_HAT,
        /**
         * When you pickup an item
         */
        PICKUP,
        /**
         * When you shift click an armor piece to equip or unequip
         */
        SHIFT_CLICK,
        /**
         * When you drag and drop the item to equip or unequip
         */
        DRAG,
        /**
         * When you manually equip or unequip the item. Use to be DRAG
         */
        PICK_DROP,
        /**
         * When you press the hotbar slot number to change your held slot
         */
        HELD,
        /**
         * When you press F to swap your main/off hand item
         */
        SWAP,
        /**
         * When you right click an armor piece in the hotbar without the inventory open to equip.
         */
        HOTBAR,
        /**
         * When you press the hotbar slot number while hovering over the armor slot to equip or unequip
         */
        HOTBAR_SWAP,
        /**
         * When you use item in hand or offhand
         */
        USE,
        /**
         * When in range of a dispenser that shoots an armor piece to equip.<br>
         * Requires the spigot version to have {@link org.bukkit.event.block.BlockDispenseArmorEvent} implemented.
         */
        DISPENSER,
        /**
         * When an armor piece is removed due to it losing all durability.
         */
        BROKE,
        /**
         * When you die causing all armor to unequip
         */
        DEATH,
        /**
         * When you drop your item in main hand
         */
        DROP,
        /**
         * Check for special purpose
         */
        CUSTOM,
        ;
    }
}