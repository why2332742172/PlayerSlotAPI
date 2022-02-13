package com.github.playerslotapi.event;

import com.github.playerslotapi.slot.PlayerSlot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * 异步槽位更新事件. 1 tick后触发, 使得槽位更新捕捉准确
 * 这个事件不能取消, 仅作为通知使用
 */
public class AsyncSlotUpdateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    /**
     * 触发原因
     */
    private final UpdateTrigger trigger;
    /**
     * 玩家
     */
    private final Player player;
    /**
     * 更新的槽位
     */
    private final PlayerSlot slot;
    /**
     * 旧装备的副本
     */
    private final ItemStack oldItem;

    /**
     * 新装备的副本
     */
    private final ItemStack newItem;


    public AsyncSlotUpdateEvent(UpdateTrigger trigger, Player player, PlayerSlot slot, ItemStack oldItem, ItemStack newItem) {
        super(true);
        this.trigger = trigger;
        this.player = player;
        this.slot = slot;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public UpdateTrigger getTrigger() {
        return trigger;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerSlot getSlot() {
        return slot;
    }

    public ItemStack getOldItem() {
        return oldItem;
    }

    public ItemStack getNewItem() {
        return newItem;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
