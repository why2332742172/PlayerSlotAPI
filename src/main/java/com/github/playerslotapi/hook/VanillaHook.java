package com.github.playerslotapi.hook;


import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.event.UpdateTrigger;
import com.github.playerslotapi.slot.PlayerSlot;
import com.github.playerslotapi.slot.impl.VanillaEquipSlot;
import com.github.playerslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.Set;

import static com.github.playerslotapi.util.Util.isAir;
import static com.github.playerslotapi.util.Util.newHashSet;


public class VanillaHook {

    private static final Set<String> BLOCKED_MATERIALS = newHashSet(
            "FURNACE", "CHEST", "TRAPPED_CHEST", "BEACON", "DISPENSER", "DROPPER", "HOPPER",
            "WORKBENCH", "ENCHANTMENT_TABLE", "ENDER_CHEST", "ANVIL", "BED_BLOCK", "FENCE_GATE",
            "SPRUCE_FENCE_GATE", "BIRCH_FENCE_GATE", "ACACIA_FENCE_GATE", "JUNGLE_FENCE_GATE",
            "DARK_OAK_FENCE_GATE", "IRON_DOOR_BLOCK", "WOODEN_DOOR", "SPRUCE_DOOR", "BIRCH_DOOR",
            "JUNGLE_DOOR", "ACACIA_DOOR", "DARK_OAK_DOOR", "WOOD_BUTTON", "STONE_BUTTON", "TRAP_DOOR",
            "IRON_TRAPDOOR", "DIODE_BLOCK_OFF", "DIODE_BLOCK_ON", "REDSTONE_COMPARATOR_OFF",
            "REDSTONE_COMPARATOR_ON", "FENCE", "SPRUCE_FENCE", "BIRCH_FENCE", "JUNGLE_FENCE",
            "DARK_OAK_FENCE", "ACACIA_FENCE", "NETHER_FENCE", "BREWING_STAND", "CAULDRON",
            "LEGACY_SIGN_POST", "LEGACY_WALL_SIGN", "LEGACY_SIGN", "ACACIA_SIGN", "ACACIA_WALL_SIGN",
            "BIRCH_SIGN", "BIRCH_WALL_SIGN", "DARK_OAK_SIGN", "DARK_OAK_WALL_SIGN", "JUNGLE_SIGN",
            "JUNGLE_WALL_SIGN", "OAK_SIGN", "OAK_WALL_SIGN", "SPRUCE_SIGN", "SPRUCE_WALL_SIGN", "LEVER",
            "BLACK_SHULKER_BOX", "BLUE_SHULKER_BOX", "BROWN_SHULKER_BOX", "CYAN_SHULKER_BOX",
            "GRAY_SHULKER_BOX", "GREEN_SHULKER_BOX", "LIGHT_BLUE_SHULKER_BOX", "LIME_SHULKER_BOX",
            "MAGENTA_SHULKER_BOX", "ORANGE_SHULKER_BOX", "PINK_SHULKER_BOX", "PURPLE_SHULKER_BOX",
            "RED_SHULKER_BOX", "SILVER_SHULKER_BOX", "WHITE_SHULKER_BOX", "YELLOW_SHULKER_BOX",
            "DAYLIGHT_DETECTOR_INVERTED", "DAYLIGHT_DETECTOR", "BARREL", "BLAST_FURNACE", "SMOKER",
            "CARTOGRAPHY_TABLE", "COMPOSTER", "GRINDSTONE", "LECTERN", "LOOM", "STONECUTTER", "BELL",
            "BEEHIVE"
    );
    private static boolean disableDropDetect = false;

    /**
     * 工具函数，用于同时触发槽位预更新和后更新事件
     *
     * @param trigger 触发原因
     * @param player  玩家
     * @param slot    槽位
     * @param oldItem 老物品
     * @param newItem 新物品
     * @return 返回槽位更新事件
     */
    private static boolean cancelSlotUpdate(UpdateTrigger trigger, Player player, PlayerSlot slot, ItemStack oldItem, ItemStack newItem) {
        if (oldItem == null) {
            oldItem = new ItemStack(Material.AIR);
        }
        if (oldItem.equals(newItem)) {
            return false;
        }
        SlotUpdateEvent update = new SlotUpdateEvent(trigger, player, slot, oldItem, newItem);
        Bukkit.getPluginManager().callEvent(update);
        // 事件已经被取消, 或者不知道将要被更新成什么, 则只触发预更新
        return update.isCancelled();
    }

    /**
     * 订阅所有事件
     */
    @SuppressWarnings("deprecation")
    public static void registerEvents() {
        Events.subscribe(InventoryClickEvent.class, VanillaHook::onInventoryClick);
        Events.subscribe(InventoryDragEvent.class, VanillaHook::onInventoryDrag);
        Events.subscribe(InventoryCloseEvent.class, VanillaHook::onInventoryClose);
        Events.subscribe(PlayerCommandPreprocessEvent.class, VanillaHook::onCommand);
        Events.subscribe(PlayerDropItemEvent.class, VanillaHook::onPlayerDropItem);
        Events.subscribe(PlayerItemHeldEvent.class, VanillaHook::onPlayerItemHeld);
        Events.subscribe(PlayerSwapHandItemsEvent.class, VanillaHook::onPlayerSwapItem);
        Events.subscribe(PlayerPickupItemEvent.class, VanillaHook::onPlayerPickupItem);
        Events.subscribe(PlayerItemDamageEvent.class, VanillaHook::onPlayerItemDamage);
        Events.subscribe(PlayerInteractEvent.class, EventPriority.HIGHEST, false, VanillaHook::onPlayerInteract);
        Events.subscribe(PlayerItemBreakEvent.class, EventPriority.MONITOR, false, VanillaHook::onPlayerItemBreak);
        try {
            Events.subscribe(BlockDispenseArmorEvent.class, event -> {
                VanillaEquipSlot slot = VanillaEquipSlot.matchType(event.getItem());
                if (slot != null && event.getTargetEntity() instanceof Player) {
                    Player player = (Player) event.getTargetEntity();
                    if (cancelSlotUpdate(UpdateTrigger.DISPENSER, player, slot, slot.get(player), event.getItem())) {
                        event.setCancelled(true);
                    }
                }
            });
        } catch (Throwable ignored) {

        }
    }

    /**
     * 检查玩家点击造成的装备和主手副手更新
     *
     * @param event GUI点击事件
     */
    private static void onInventoryClick(final InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.NOTHING) {
            // 点击无事发生 直接返回
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getClickedInventory() == null) {
            // 点击栏外 直接返回
            if (event.getAction() == InventoryAction.DROP_ALL_CURSOR || event.getAction() == InventoryAction.DROP_ONE_CURSOR) {
                disableDropDetect = true;
            }
            return;
        }
        if (event.getAction() == InventoryAction.DROP_ALL_SLOT || event.getAction() == InventoryAction.DROP_ONE_SLOT) {
            // 鼠标有物品的时候按Q丢不了东西, InventoryAction却又不是NOTHING
            // 此乃Bukkit缺陷, 需自己解决
            if (!isAir(event.getCursor())) {
                return;
            }
            disableDropDetect = true;
        }
        boolean shift = false;
        boolean numberkey = false;
        if (event.getClick().equals(ClickType.SHIFT_LEFT) || event.getClick().equals(ClickType.SHIFT_RIGHT)) {
            shift = true;
        }
        if (event.getClick().equals(ClickType.NUMBER_KEY)) {
            numberkey = true;
        }
        Player player = (Player) event.getWhoClicked();
        // 处理主手变化情况
        if (numberkey) {
            // 数字键切换, 来源去向都检查
            // 数字键可能同时产生主手变化和装备变化 此处只检查主手
            int hotbarId = event.getHotbarButton();
            int targetId = event.getSlot();
            if (hotbarId == player.getInventory().getHeldItemSlot()) {
                if (cancelSlotUpdate(UpdateTrigger.HOTBAR_SWAP, player, VanillaEquipSlot.MAINHAND, player.getInventory().getItem(hotbarId), event.getCurrentItem())) {
                    event.setCancelled(true);
                    return;
                }
            } else if (targetId == player.getInventory().getHeldItemSlot()) {
                if (cancelSlotUpdate(UpdateTrigger.HOTBAR_SWAP, player, VanillaEquipSlot.MAINHAND, event.getCurrentItem(), player.getInventory().getItem(hotbarId))) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else {
            // 直接点击
            // shift点击储存区和副手时，有可能直接把装备送进格子里，从而无需检查主手
            // 直接在这里处理shift左键快捷装备, 如果装备了通知主副手然后短路处理
            boolean equipped = false;
            if (shift && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                VanillaEquipSlot quickEquipSlot = VanillaEquipSlot.matchType(event.getCurrentItem());
                // 成功装备上的情况
                if (quickEquipSlot != null && isAir(quickEquipSlot.get(player)) && (event.getSlotType() == SlotType.CONTAINER || event.getSlotType() == SlotType.QUICKBAR)) {
                    if (cancelSlotUpdate(UpdateTrigger.SHIFT_CLICK, player, quickEquipSlot, null, event.getCurrentItem())) {
                        event.setCancelled(true);
                        return;
                    }
                    // 通知副手 准备短路处理
                    if (event.getRawSlot() == 45 && cancelSlotUpdate(UpdateTrigger.SHIFT_CLICK, player, VanillaEquipSlot.OFFHAND, event.getCurrentItem(), new ItemStack(Material.AIR))) {
                        event.setCancelled(true);
                        return;
                    }
                    equipped = true;
                }
            }
            if (event.getSlotType() == SlotType.QUICKBAR && (event.getRawSlot() != 45 || event.getClickedInventory().getType() != InventoryType.PLAYER)) {
                // 点击热键栏且不是副手位置 检查
                if (event.getSlot() == player.getInventory().getHeldItemSlot()) {
                    // 如果点击的是主手位置
                    // 如果按了shift, 物品栏里面又没有相似物品或者空位, 返回
                    if (shift) {
                        if (!equipped) {
                            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                            int left = mainHandItem.getAmount();
                            for (int i = 9; i < 36; i++) {
                                ItemStack possible = player.getInventory().getItem(i);
                                if (isAir(possible)) {
                                    left = 0;
                                    break;
                                }
                                if (possible.isSimilar(mainHandItem) && possible.getAmount() < possible.getMaxStackSize()) {
                                    left -= (possible.getMaxStackSize() - possible.getAmount());
                                    if (left <= 0) {
                                        left = 0;
                                        break;
                                    }
                                }
                            }
                            if (left == mainHandItem.getAmount()) {
                                return;
                            }
                            ItemStack newItem;
                            if (left == 0) {
                                newItem = new ItemStack(Material.AIR);
                            } else {
                                newItem = mainHandItem.clone();
                                newItem.setAmount(left);
                            }
                            if (cancelSlotUpdate(UpdateTrigger.SHIFT_CLICK, player, VanillaEquipSlot.MAINHAND, event.getCurrentItem(), newItem)) {
                                event.setCancelled(true);
                            }
                        } else if (cancelSlotUpdate(UpdateTrigger.SHIFT_CLICK, player, VanillaEquipSlot.MAINHAND, event.getCurrentItem(), new ItemStack(Material.AIR))) {
                            event.setCancelled(true);
                        }
                    } else {
                        ItemStack newItem;
                        if (event.getAction() == InventoryAction.DROP_ONE_SLOT) {
                            newItem = event.getCurrentItem().clone();
                            newItem.setAmount(newItem.getAmount() - 1);
                        } else if (event.getAction() == InventoryAction.DROP_ALL_SLOT) {
                            newItem = new ItemStack(Material.AIR);
                        } else {
                            newItem = event.getCursor();
                        }
                        if (cancelSlotUpdate(UpdateTrigger.PICK_DROP, player, VanillaEquipSlot.MAINHAND, event.getCurrentItem(),
                                newItem)) {
                            event.setCancelled(true);
                        }
                    }
                    return;
                }
            } else if (shift && !equipped) {
                // 检查点击热键栏之外的位置是否把物品送到了主手
                // 之前检查过快捷装备, 如果快捷装备成功此处就不检查了
                ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                ItemStack item = event.getCurrentItem();
                // 如果主手物品为空或和点击物品相似 则进行检查
                if (isAir(mainHandItem) || mainHandItem.isSimilar(item) && mainHandItem.getMaxStackSize() > mainHandItem.getAmount()) {
                    PlayerInventory inventory = player.getInventory();
                    int mainHandIndex = inventory.getHeldItemSlot();
                    int amount = item.getAmount();
                    // 如果是副手点击则先填充Container的东西
                    boolean abort = false;
                    if (event.getClickedInventory().getType() == InventoryType.PLAYER && event.getRawSlot() == 45) {
                        for (int i = 9; i < 36; i++) {
                            ItemStack possible = inventory.getItem(i);
                            if (item.isSimilar(possible) && possible.getMaxStackSize() > possible.getAmount()) {
                                amount -= (possible.getMaxStackSize() - possible.getAmount());
                                if (amount <= 0) {
                                    abort = true;
                                    break;
                                }
                            }
                        }
                    }
                    // 填充主手前面的快捷栏
                    if (!abort) {
                        for (int i = 0; i < mainHandIndex; i++) {
                            ItemStack possible = inventory.getItem(i);
                            if (item.isSimilar(possible) && possible.getMaxStackSize() > possible.getAmount()) {
                                amount -= (possible.getMaxStackSize() - possible.getAmount());
                                if (amount <= 0) {
                                    abort = true;
                                    break;
                                }
                            }
                        }
                    }
                    // 如果主手是空气
                    if (!abort && isAir(mainHandItem)) {
                        // 接着填充快捷栏
                        for (int i = mainHandIndex + 1; i < 9; i++) {
                            ItemStack possible = inventory.getItem(i);
                            if (item.isSimilar(possible) && possible.getMaxStackSize() > possible.getAmount()) {
                                amount -= (possible.getMaxStackSize() - possible.getAmount());
                                if (amount <= 0) {
                                    abort = true;
                                    break;
                                }
                            }
                        }
                        // 如果点击了副手槽位且储存区有空气 放弃
                        if (!abort && event.getClickedInventory().getType() == InventoryType.PLAYER && event.getRawSlot() == 45) {
                            for (int i = 9; i < 36; i++) {
                                if (isAir(inventory.getItem(i))) {
                                    abort = true;
                                    break;
                                }
                            }
                        }
                        // 如果主手前面有空气 放弃
                        if (!abort) {
                            for (int i = 0; i < mainHandIndex; i++) {
                                if (isAir(inventory.getItem(i))) {
                                    abort = true;
                                    break;
                                }
                            }
                        }
                    }
                    // 如果没有放弃则触发事件
                    if (!abort) {
                        ItemStack newItem = item.clone();
                        newItem.setAmount(Math.min((isAir(mainHandItem) ? 0 : mainHandItem.getAmount()) + amount, mainHandItem.getMaxStackSize()));
                        if (cancelSlotUpdate(UpdateTrigger.PICKUP, player, VanillaEquipSlot.MAINHAND, mainHandItem, newItem)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
            // 如果是shift左键快捷装备, 此时已经检查完毕
            // 短路处理
            if (equipped) {
                // 延迟后更新事件在此处提交Scheduler
                return;
            }
        }
        // 主手处理完毕 现在处理副手和四个装备格子
        // 这五个格子只会在PlayerInventory中发生改变
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
            return;
        }
        // 快速穿装备已经被检查了。所以只剩下直接穿脱和shift脱, 不用检查其它格子
        VanillaEquipSlot slot = VanillaEquipSlot.getById(event.getRawSlot());
        if (slot == null) {
            return;
        }
        boolean result;
        if (shift) {
            // shift点击快速脱
            // 如果物品栏没空位就不用检查了
            if (player.getInventory().firstEmpty() == -1) {
                return;
            }
            result = !cancelSlotUpdate(UpdateTrigger.SHIFT_CLICK,
                    player, slot, event.getCurrentItem(), new ItemStack(Material.AIR));
            // 点击其它位置的shift已经被检查过了 直接省略
        } else {
            // 取得新物品
            ItemStack newItem;
            if (numberkey) {
                newItem = event.getClickedInventory().getItem(event.getHotbarButton());
                newItem = newItem == null ? new ItemStack(Material.AIR) : newItem;
            } else {
                if (event.getAction() == InventoryAction.DROP_ONE_SLOT) {
                    newItem = event.getCurrentItem().clone();
                    newItem.setAmount(newItem.getAmount() - 1);
                } else if (event.getAction() == InventoryAction.DROP_ALL_SLOT) {
                    newItem = new ItemStack(Material.AIR);
                } else {
                    newItem = event.getCursor();
                }
            }
            // 如果槽位不是副手, 那么交换需要检查是否放的进去
            if (slot != VanillaEquipSlot.OFFHAND) {
                // 空气肯定能放进去
                if (newItem == null) {
                    newItem = new ItemStack(Material.AIR);
                } else if (newItem.getType() != Material.AIR && !slot.equals(VanillaEquipSlot.matchType(newItem))) {
                    return;
                }
            }
            result = !cancelSlotUpdate(event.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey ? UpdateTrigger.HOTBAR_SWAP : UpdateTrigger.PICK_DROP,
                    player, slot, event.getCurrentItem(), newItem);
        }
        if (!result) {
            event.setCancelled(true);
        }
    }

    /**
     * 检查玩家右键导致的快速装备穿戴和主手副手更新
     *
     * @param event 玩家交互事件
     */
    private static void onPlayerInteract(PlayerInteractEvent event) {
        // 如果手中物品为空或者无法使用, 无需继续检查
        ItemStack current = event.getItem();
        if (isAir(current) || event.useItemInHand().equals(Result.DENY)) {
            return;
        }
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            // 如果可以与方块交互
            if (!event.useInteractedBlock().equals(Result.DENY) && event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {// Having both of these checks is useless, might as well do it though.
                // 有些方块右键时候打开GUI
                // 这时候啥也不会触发
                String name = event.getClickedBlock().getType().toString().toUpperCase();
                if (BLOCKED_MATERIALS.contains(name)) {
                    return;
                }
            }
            final VanillaEquipSlot handSlot = event.getHand() == EquipmentSlot.HAND ? VanillaEquipSlot.MAINHAND : VanillaEquipSlot.OFFHAND;
            VanillaEquipSlot quickEquipSlot = VanillaEquipSlot.matchType(event.getItem());
            if (quickEquipSlot != null && quickEquipSlot != VanillaEquipSlot.OFFHAND && isAir(quickEquipSlot.get(player))) {
                // 如果成功快速装备, 则顺便把主副手也给通知了, 然后短路处理
                boolean equipResult = !cancelSlotUpdate(UpdateTrigger.HOTBAR, player, quickEquipSlot, null, current);
                boolean handResult = !cancelSlotUpdate(UpdateTrigger.USE, player, handSlot, current, new ItemStack(Material.AIR));
                if (!equipResult || !handResult) {
                    event.setCancelled(true);
                }
                return;
            }
            // 如果触发了使用
            // 检查触发事件的手
            if (cancelSlotUpdate(UpdateTrigger.USE, player, handSlot, current, null)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 检查拖曳事件造成的原版装备格子更新
     *
     * @param event 装备格子更新事件
     */
    private static void onInventoryDrag(InventoryDragEvent event) {
        if (event.getRawSlots().isEmpty()) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Map<Integer, ItemStack> items = event.getNewItems();
        int mainHandIndex = player.getInventory().getHeldItemSlot();
        int topSize = event.getView().getTopInventory().getSize();
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            if (entry.getKey() < topSize) {
                continue;
            }
            if (mainHandIndex == event.getView().convertSlot(entry.getKey())) {
                if (cancelSlotUpdate(UpdateTrigger.DRAG, player, VanillaEquipSlot.MAINHAND, event.getView().getItem(entry.getKey()), entry.getValue())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        final int[] slots = {5, 6, 7, 8, 45};
        for (int i : slots) {
            if (items.containsKey(i)) {
                VanillaEquipSlot slot = VanillaEquipSlot.getById(i);
                if (cancelSlotUpdate(UpdateTrigger.DRAG, player, slot, slot.get(player), items.get(i))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * 检查装备耐久损失导致的装备更新
     * 依次匹配四个装备格子和主副手
     *
     * @param event 装备耐久变化事件
     */
    @SuppressWarnings("deprecation")
    private static void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (event.getDamage() == 0) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        VanillaEquipSlot slot = VanillaEquipSlot.matchType(item);
        // 如果装备成功匹配到类型, 则肯定是装备或者盾牌, 直接callEvent
        if (slot != null) {
            ItemStack newItem = item.clone();
            newItem.setDurability((short) (item.getDurability() + event.getDamage()));
            if (cancelSlotUpdate(UpdateTrigger.DAMAGED, event.getPlayer(), slot, slot.get(player), newItem)) {
                event.setCancelled(true);
            }
            return;
        }
        // 否则检查主手和副手的物品 哪个和当前物品完全一致
        // 先检查主手 一样就不检查副手了
        ItemStack main = player.getInventory().getItemInMainHand();
        if (item.equals(main)) {
            ItemStack newItem = item.clone();
            newItem.setDurability((short) (item.getDurability() + event.getDamage()));
            if (cancelSlotUpdate(UpdateTrigger.DAMAGED, event.getPlayer(), VanillaEquipSlot.MAINHAND, main, newItem)) {
                event.setCancelled(true);
            }
            return;
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (item.equals(off)) {
            ItemStack newItem = item.clone();
            newItem.setDurability((short) (item.getDurability() + event.getDamage()));
            if (cancelSlotUpdate(UpdateTrigger.DAMAGED, event.getPlayer(), VanillaEquipSlot.OFFHAND, off, newItem)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 检查装备损坏
     *
     * @param event 装备损坏事件
     */
    private static void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getBrokenItem();
        ItemStack newItem = new ItemStack(Material.AIR);
        VanillaEquipSlot slot = VanillaEquipSlot.matchType(item);
        // 检查是否是已知槽位
        if (slot != null) {
            if (cancelSlotUpdate(UpdateTrigger.BROKE, player, slot, item, newItem)) {
                slot.set(player, item);
            }
            return;
        }
        // 否则检查主手和副手的物品 哪个和当前物品完全一致
        // 先检查主手 一样就不检查副手了
        ItemStack main = player.getInventory().getItemInMainHand();
        if (item.equals(main)) {
            if (cancelSlotUpdate(UpdateTrigger.BROKE,
                    player, VanillaEquipSlot.MAINHAND, item, newItem)) {
                player.getInventory().setItemInMainHand(item);
            }
            return;
        }
        ItemStack off = player.getInventory().getItemInOffHand();
        if (item.equals(off)) {
            if (cancelSlotUpdate(UpdateTrigger.BROKE,
                    player, VanillaEquipSlot.OFFHAND, item, newItem)) {
                player.getInventory().setItemInOffHand(item);
            }
        }
    }

    /**
     * 检查主副手交换事件
     * 因为可以短路的地方较多, 所以手工构造事件并触发
     *
     * @param event 玩家主副手交换事件
     */
    private static void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = event.getMainHandItem();
        ItemStack offHandItem = event.getOffHandItem();
        if (mainHandItem == null) {
            mainHandItem = new ItemStack(Material.AIR);
        }
        if (offHandItem == null) {
            offHandItem = new ItemStack(Material.AIR);
        }
        if (cancelSlotUpdate(UpdateTrigger.SWAP, player, VanillaEquipSlot.MAINHAND, offHandItem, mainHandItem)
                || cancelSlotUpdate(UpdateTrigger.SWAP, player, VanillaEquipSlot.OFFHAND, mainHandItem, offHandItem)) {
            event.setCancelled(true);
        }
    }

    /**
     * 检查玩家主手更替
     *
     * @param event 玩家主手更替事件
     */
    private static void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack oldItem = inventory.getItem(event.getPreviousSlot());
        ItemStack newItem = inventory.getItem(event.getNewSlot());
        if (newItem == null) {
            newItem = new ItemStack(Material.AIR);
        }
        if (cancelSlotUpdate(UpdateTrigger.HELD, player, VanillaEquipSlot.MAINHAND, oldItem, newItem)) {
            event.setCancelled(true);
        }
    }


    /**
     * 关闭GUI时如果鼠标上还有东西, 暂时禁止掉落检测
     *
     * @param event GUI关闭事件
     */
    private static void onInventoryClose(InventoryCloseEvent event) {
        if (!isAir(event.getPlayer().getItemOnCursor())) {
            disableDropDetect = true;
        }
    }

    /**
     * 检查玩家丢弃物品事件
     *
     * @param event 玩家丢弃物品
     */
    private static void onPlayerDropItem(PlayerDropItemEvent event) {
        // InventoryClick 和 InventoryClose 引发的Drop事件, 忽略之, 不处理
        if (disableDropDetect) {
            disableDropDetect = false;
            return;
        }
        Player player = event.getPlayer();
        // 鼠标有物品时是丢弃鼠标物品, 不触发检查
        ItemStack newItem = player.getInventory().getItemInMainHand();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        ItemStack oldItem;
        if (isAir(newItem)) {
            newItem = new ItemStack(Material.AIR);
            oldItem = droppedItem;
        } else {
            oldItem = newItem.clone();
            oldItem.setAmount(newItem.getAmount() + droppedItem.getAmount());
        }
        if (cancelSlotUpdate(UpdateTrigger.DROP, player, VanillaEquipSlot.MAINHAND, oldItem, newItem)) {
            event.setCancelled(true);
        }
    }

    /**
     * 检查玩家捡起装备产生的主手更新
     * 用PlayerPickup是为了1.9x兼容
     *
     * @param event 玩家拾取装备事件
     */
    @SuppressWarnings("deprecation")
    private static void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack item = event.getItem().getItemStack();
        ItemStack mainHand = inventory.getItemInMainHand();
        // 如果主手有物品
        if (!isAir(mainHand)) {
            // 当且仅当相似且可堆叠时触发检查
            if (mainHand.isSimilar(item) && mainHand.getAmount() < mainHand.getMaxStackSize()) {
                ItemStack newItem = item.clone();
                newItem.setAmount(Math.min(mainHand.getAmount() + newItem.getAmount(), newItem.getMaxStackSize()));
                if (cancelSlotUpdate(UpdateTrigger.PICKUP, player, VanillaEquipSlot.MAINHAND, mainHand, newItem)) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        // 如果主手没有物品
        // 如果主手之前有空位, 返回
        for (int i = 0; i < inventory.getHeldItemSlot(); i++) {
            ItemStack current = inventory.getItem(i);
            if (isAir(current)) {
                return;
            }
        }
        // 否则优先给相似槽位放置
        int amount = item.getAmount();
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack possible : contents) {
            if (item.isSimilar(possible)) {
                amount -= (item.getMaxStackSize() - possible.getAmount());
                if (amount <= 0) {
                    return;
                }
            }
        }
        // 此时主手更新了
        ItemStack newItem = item.clone();
        newItem.setAmount(amount);
        if (cancelSlotUpdate(UpdateTrigger.PICKUP, player, VanillaEquipSlot.MAINHAND, null, newItem)) {
            event.setCancelled(true);
        }
    }

    private static void onCommand(PlayerCommandPreprocessEvent event) {
        // 出于兼容性考虑
        // hat指令将触发头盔的更新
        // 其它指令则仅仅异步检查主手
        Player player = event.getPlayer();
        ItemStack mainhand = player.getInventory().getItemInMainHand();
        if (isAir(mainhand)) {
            return;
        }
        if (event.getMessage().toLowerCase().startsWith("/hat")) {
            if (mainhand.getType().isBlock() && cancelSlotUpdate(UpdateTrigger.COMMAND_HAT, player, VanillaEquipSlot.HELMET, player.getInventory().getHelmet(), mainhand)) {
                event.setCancelled(true);
            }
            return;
        }
        if (cancelSlotUpdate(UpdateTrigger.COMMAND, player, VanillaEquipSlot.MAINHAND, mainhand, null)) {
            event.setCancelled(true);
        }
    }
}
