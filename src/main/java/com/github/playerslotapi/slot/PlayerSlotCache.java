package com.github.playerslotapi.slot;

import com.github.playerslotapi.PlayerSlotAPI;
import com.github.playerslotapi.event.AsyncSlotUpdateEvent;
import com.github.playerslotapi.event.UpdateTrigger;
import com.github.playerslotapi.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSlotCache {


    private static final ItemStack AIR = new ItemStack(Material.AIR);

    /**
     * 缓存的槽位物品和数据. 默认是
     */
    private final Map<PlayerSlot, ItemStack> itemCache = new ConcurrentHashMap<>(6);

    /**
     * 异步修改专用槽. 一般不会有太多数据所以初始大小为1
     */
    private final Map<PlayerSlot, ItemStack> verifyCache = new ConcurrentHashMap<>(1);
    private final Map<PlayerSlot, ItemStack> modifiedCache = new ConcurrentHashMap<>(1);

    private final Player player;

    public PlayerSlotCache(Player player) {
        this.player = player;
        for (PlayerSlot slot : PlayerSlotAPI.getAPI().getSlotMap().values()) {
            initSlot(slot);
        }
    }

    /**
     * 加载槽位到缓存中.
     * 初始化为全同步过程, 无视缓存
     *
     * @param slot 要加载的槽位
     */
    public void initSlot(PlayerSlot slot) {
        slot.get(player, item -> {
            item = item == null ? new ItemStack(Material.AIR) : item.clone();
            itemCache.put(slot, item);
        });
    }


    /**
     * 获取槽位中缓存的物品
     *
     * @param slot 要获取的槽位
     * @return 缓存物品
     */
    @NotNull
    public ItemStack getItem(PlayerSlot slot) {
        return itemCache.getOrDefault(slot, AIR);
    }

    /**
     * 获取异步修改槽位中的物品
     * 如果是第一次获取, 顺便储存修改前的槽位状态
     *
     * @param slot 槽位
     * @return 异步修改后的物品
     */
    @NotNull
    public ItemStack getModifiedItem(PlayerSlot slot) {
        ItemStack modifiedItem = modifiedCache.get(slot);
        if (modifiedItem == null) {
            modifiedItem = getItem(slot);
            verifyCache.put(slot, modifiedItem);
        }
        return modifiedItem;
    }

    /**
     * 对槽位进行异步修改，将结果缓存
     * 你必须要在这个方法调用前调用getModifiedItem
     *
     * @param slot 槽位
     * @param item 新的异步修改后的物品
     */
    public void setModifiedItem(PlayerSlot slot, ItemStack item) {
        modifiedCache.put(slot, item);
    }

    /**
     * 将异步修改应用到玩家槽位中
     * 如果玩家槽位在异步计算期间已经改变
     * 那么修改不起作用 防止刷物品
     * 此方法必须同步调用
     *
     * @param ignoreDamage 是否忽略耐久变化
     */
    public void applyModification(boolean ignoreDamage) {
        final List<Map.Entry<PlayerSlot, ItemStack>> slots = new ArrayList<>(modifiedCache.entrySet());
        final Map<PlayerSlot, ItemStack> tempVerifyCache = new ConcurrentHashMap<>(1);
        tempVerifyCache.putAll(verifyCache);
        modifiedCache.clear();
        verifyCache.clear();
        for (Map.Entry<PlayerSlot, ItemStack> entry : slots) {
            PlayerSlot slot = entry.getKey();
            slot.get(player, truth -> {
                if (Util.isAir(truth)) {
                    // 禁止修改空气装备
                    return;
                }
                ItemStack verify = tempVerifyCache.get(slot);
                if (ignoreDamage) {
                    if (verify.getType() != truth.getType() || verify.getAmount() != truth.getAmount() || !Bukkit.getItemFactory().equals(verify.getItemMeta(), truth.getItemMeta())) {
                        return;
                    }
                } else if (verify.equals(truth)) {
                    return;
                }
                if (Bukkit.isPrimaryThread() || slot.isAsyncSafe()) {
                    slot.set(player, entry.getValue());
                } else {
                    Bukkit.getScheduler().runTask(PlayerSlotAPI.getPlugin(), () -> {
                        slot.set(player, entry.getValue());
                    });
                }
            });
        }
    }

    /**
     * 尝试更新槽位物品
     * 注意这个方法会被频繁调用
     *
     * @param slot 槽位类型
     * @param item 槽位物品
     */
    public void updateItem(UpdateTrigger trigger, PlayerSlot slot, ItemStack item) {
        final ItemStack oldItem = itemCache.getOrDefault(slot, AIR);
        final ItemStack newItem = item == null ? new ItemStack(Material.AIR) : item.clone();
        Bukkit.getScheduler().runTaskAsynchronously(PlayerSlotAPI.getPlugin(), () -> {
            if (oldItem.equals(newItem)) {
                return;
            }
            AsyncSlotUpdateEvent asyncEvent = new AsyncSlotUpdateEvent(trigger, player, slot, oldItem, newItem);
            Bukkit.getPluginManager().callEvent(asyncEvent);
            itemCache.put(slot, newItem);
        });
    }

    /**
     * 更新所有槽位
     */
    public void updateAll() {
        for (PlayerSlot slot : PlayerSlotAPI.getAPI().getSlotMap().values()) {
            slot.get(player, item -> updateItem(UpdateTrigger.CUSTOM, slot, item));
        }
    }
}
