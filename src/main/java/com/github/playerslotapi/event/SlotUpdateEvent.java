package com.github.playerslotapi.event;

import com.github.playerslotapi.slot.PlayerSlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 装备更新事件
 * 当玩家槽位发生装备更新时触发
 */
public class SlotUpdateEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    /**
     * 触发原因
     */
    private final UpdateTrigger trigger;
    /**
     * 触发槽位
     */
    private final PlayerSlot slot;
    /**
     * 槽位中原先的装备
     */
    private final ItemStack oldItem;
    /**
     * 槽位中的新装备. 当且仅当API不能事先得知槽位中是什么装备时, 这个物品为null
     */
    private final ItemStack newItem;
    /**
     * 取消状态. 取消时槽位装备将不会发生更新
     */
    private boolean isCancelled;

    /**
     * 是否立即更新. 对于原版槽位, 延时更新能使得更新更准确
     * 但对于自定义槽位而言就没有必要了
     */
    private boolean immediate = false;

    public SlotUpdateEvent(UpdateTrigger trigger, Player player, PlayerSlot slot, ItemStack oldItem, ItemStack newItem) {
        super(player);
        this.trigger = trigger;
        this.slot = slot;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ItemStack getOldItem() {
        return oldItem == null ? new ItemStack(Material.AIR) : oldItem;
    }

    @Nullable
    public ItemStack getNewItem() {
        return newItem;
    }

    public UpdateTrigger getTrigger() {
        return trigger;
    }

    public PlayerSlot getSlot() {
        return slot;
    }

    public boolean isUpdateImmediately(){
        return immediate;
    }

    public void setUpdateImmediately(){
        immediate = true;
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
