package com.github.playerslotapi.event;

public enum UpdateTrigger {
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
     * When you are damaged
     */
    DAMAGED,
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
    /**
     * DragonCore slot
     */
    DRAGON_CORE,
    /**
     * GermPlugin slot
     */
    GERM_PLUGIN
    ;
}
