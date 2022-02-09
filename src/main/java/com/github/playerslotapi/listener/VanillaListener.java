package com.github.playerslotapi.listener;


import com.github.playerslotapi.event.CheckEquipEvent;
import com.github.playerslotapi.event.CheckEquipEvent.CheckTrigger;
import com.github.playerslotapi.slot.impl.VanillaSlot;
import com.github.playerslotapi.util.Events;
import com.google.common.collect.Sets;
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

import java.util.Set;


public class VanillaListener {

    private static final Set<String> BLOCKED_MATERIALS = Sets.newHashSet(
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
            "CARTOGRAPHY_TABLE", "COMPOSTER", "GRINDSTONE", "LECTERN", "LOOM", "STONECUTTER", "BELL"
    );

    @SuppressWarnings("deprecation")
    public static void registerEvents() {
        Events.subscribe(PlayerCommandPreprocessEvent.class, VanillaListener::onHatCommand);
        Events.subscribe(InventoryClickEvent.class, VanillaListener::onInventoryClick);
        Events.subscribe(PlayerDropItemEvent.class, VanillaListener::onPlayerDropItem);
        Events.subscribe(InventoryDragEvent.class, VanillaListener::onInventoryDrag);
        Events.subscribe(PlayerInteractEvent.class, VanillaListener::onPlayerInteract);
        Events.subscribe(PlayerItemHeldEvent.class, VanillaListener::onPlayerItemHeld);
        Events.subscribe(PlayerSwapHandItemsEvent.class, VanillaListener::onPlayerSwapItem);
        Events.subscribe(PlayerPickupItemEvent.class, VanillaListener::onPlayerPickupItem);
        Events.subscribe(PlayerDropItemEvent.class, VanillaListener::onPlayerDropItem);
        Events.subscribe(PlayerItemBreakEvent.class, EventPriority.MONITOR, false, VanillaListener::onPlayerItemBreak);
        try {
            Events.subscribe(BlockDispenseArmorEvent.class, event -> {
                VanillaSlot type = VanillaSlot.matchType(event.getItem());
                if (type != null && event.getTargetEntity() instanceof Player) {
                    Player p = (Player) event.getTargetEntity();
                    CheckEquipEvent checkEquipEvent = new CheckEquipEvent(p, CheckEquipEvent.CheckTrigger.DISPENSER, type);
                    Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
                }
            });
        } catch (Throwable ignored) {

        }
    }

    private static boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    private static void onInventoryClick(final InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getAction() == InventoryAction.NOTHING) {
            return;// 点击无事发生 直接返回
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
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
        CheckEquipEvent mainEquipEvent = null;
        if (numberkey) {
            // 数字键切换, 来源去向都检查
            int hotbarID = event.getHotbarButton();
            int targetID = event.getSlot();
            if (targetID == player.getInventory().getHeldItemSlot() || hotbarID == player.getInventory().getHeldItemSlot()) {
                mainEquipEvent = new CheckEquipEvent(player, CheckTrigger.HOTBAR_SWAP, VanillaSlot.MAINHAND);
            }
        } else {
            // 点击热键栏且点击位置为主手位 触发检查
            if (event.getSlotType() == SlotType.QUICKBAR) {
                if (event.getSlot() == player.getInventory().getHeldItemSlot()) {
                    mainEquipEvent = new CheckEquipEvent(player, (shift ? CheckTrigger.SHIFT_CLICK : CheckTrigger.PICK_DROP), VanillaSlot.MAINHAND);
                }
            } else if (shift) {
                // 点击非热键栏但按了SHIFT 也触发检查
                mainEquipEvent = new CheckEquipEvent(player, CheckTrigger.SHIFT_CLICK, VanillaSlot.MAINHAND);
            }
        }
        if (mainEquipEvent != null) {
            Bukkit.getPluginManager().callEvent(mainEquipEvent);
        }
        // 主手处理完毕 现在处理副手和四个装备格子
        // 这五个格子只会在PlayerInventory中发生改变
        if (event.getSlotType() != SlotType.ARMOR && event.getSlotType() != SlotType.QUICKBAR && event.getSlotType() != SlotType.CONTAINER) {
            return;
        }
        if (event.getClickedInventory() != null && !event.getClickedInventory().getType().equals(InventoryType.PLAYER) && !event.getClickedInventory().getType().equals(InventoryType.CRAFTING)) {
            return;
        }
        if (!event.getInventory().getType().equals(InventoryType.CRAFTING) && !event.getInventory().getType().equals(InventoryType.PLAYER)) {
            return;
        }
        VanillaSlot quickEquipSlotType = VanillaSlot.matchType(shift ? event.getCurrentItem() : event.getCursor());
        CheckEquipEvent armorEquipEvent = null;
        if (shift) {
            // shift点击
            ItemStack equipItem = event.getCurrentItem();
            if (isAir(equipItem)) {
                return;
            }
            int slot = event.getRawSlot();
            if (slot < 9 || slot == 45) {
                if (slot >= 5) {
                    // 点击装备区, 卸下装备
                    VanillaSlot slotType = VanillaSlot.getById(slot);
                    armorEquipEvent = new CheckEquipEvent(player, CheckTrigger.SHIFT_CLICK, slotType);
                }
            } else {
                // 点击储存区和热键栏
                if (quickEquipSlotType != null && isAir(quickEquipSlotType.get(player))) {
                    // 成功快捷穿上装备
                    armorEquipEvent = new CheckEquipEvent(player, CheckTrigger.SHIFT_CLICK, quickEquipSlotType);
                }
            }
            if (armorEquipEvent != null) {
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            }
        } else {
            ItemStack newArmorPiece = event.getCursor();
            ItemStack oldArmorPiece = event.getCurrentItem();
            if (event.getRawSlot() == VanillaSlot.OFFHAND.getId()) {
                quickEquipSlotType = VanillaSlot.OFFHAND;
            } else {
                if (numberkey) {
                    int hotbarID = event.getHotbarButton();
                    ItemStack hotbarItem = event.getClickedInventory().getItem(hotbarID);
                    if (!isAir(hotbarItem)) {// Equipping
                        quickEquipSlotType = VanillaSlot.matchType(hotbarItem);
                    } else {// Unequipping
                        quickEquipSlotType = VanillaSlot.matchType(!isAir(event.getCurrentItem()) ? event.getCurrentItem() : event.getCursor());
                    }
                } else {
                    if (isAir(newArmorPiece) && !isAir(oldArmorPiece)) {
                        quickEquipSlotType = VanillaSlot.matchType(oldArmorPiece);
                    } else {
                        quickEquipSlotType = VanillaSlot.matchType(newArmorPiece);
                    }
                }
                if (quickEquipSlotType != VanillaSlot.getById(event.getRawSlot())) {
                    quickEquipSlotType = null;
                }
            }
            if (quickEquipSlotType != null) {
                CheckTrigger trigger = CheckTrigger.PICK_DROP;
                if (event.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey) {
                    trigger = CheckTrigger.HOTBAR_SWAP;
                }
                armorEquipEvent = new CheckEquipEvent(player, trigger, quickEquipSlotType);
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            }
        }
    }

    // 这个事件处理器会强制进行主或副手检查
    private static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useItemInHand().equals(Result.DENY)) {
            return;
        }
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (!event.useInteractedBlock().equals(Result.DENY)) {
                if (event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {// Having both of these checks is useless, might as well do it though.
                    // Some blocks have actions when you right click them which stops the client from equipping the armor in hand.
                    String name = event.getClickedBlock().getType().toString().toUpperCase();
                    if (BLOCKED_MATERIALS.contains(name)) {
                        return;
                    }
                }
            }
            CheckEquipEvent checkEquipEvent;
            VanillaSlot newVanillaSlot = VanillaSlot.matchType(event.getItem());
            if (newVanillaSlot != null && newVanillaSlot != VanillaSlot.OFFHAND && isAir(newVanillaSlot.get(player))) {
                checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.HOTBAR, VanillaSlot.matchType(event.getItem()));
                Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
            }
            // 检查触发事件的手
            if (event.getHand() == EquipmentSlot.HAND) {
                checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.USE, VanillaSlot.MAINHAND);
            } else {
                checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.USE, VanillaSlot.OFFHAND);
            }
            Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
        }
    }

    // 这个事件处理器会强制进行主副手检查
    private static void onInventoryDrag(InventoryDragEvent event) {
        VanillaSlot type = VanillaSlot.matchType(event.getOldCursor());
        if (event.getRawSlots().isEmpty()) {
            return;// Idk if this will ever happen
        }
        Player player = (Player) event.getWhoClicked();
        CheckEquipEvent checkEquipEvent;
        if (type != null && type != VanillaSlot.OFFHAND && type.getId() == event.getRawSlots().stream().findFirst().orElse(0)) {
            checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.DRAG, type);
            Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
        }
        // 如果拖曳的格子包含主副手 检查之
        if (event.getRawSlots().contains(player.getInventory().getHeldItemSlot())) {
            checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.DRAG, VanillaSlot.MAINHAND);
            Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
        }
        if (event.getRawSlots().contains(45)) {
            checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.DRAG, VanillaSlot.OFFHAND);
            Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
        }
    }

    // 这个事件处理器会强制进行主副手检查
    private static void onPlayerItemBreak(PlayerItemBreakEvent event) {
        VanillaSlot type = VanillaSlot.matchType(event.getBrokenItem());
        Player player = event.getPlayer();
        CheckEquipEvent checkEquipEvent;
        if (type != null && type != VanillaSlot.OFFHAND) {
            checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.BROKE, type);
            Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
        }
        checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.BROKE, VanillaSlot.MAINHAND);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
        checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.BROKE, VanillaSlot.OFFHAND);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
    }

    private static void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        CheckEquipEvent checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.SWAP, VanillaSlot.MAINHAND);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
        checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.SWAP, VanillaSlot.OFFHAND);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
    }

    private static void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        CheckEquipEvent checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.HELD, VanillaSlot.MAINHAND);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
    }

    private static void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isAir(player.getInventory().getItemInMainHand())) {
            return;
        }
        CheckEquipEvent checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.DROP, VanillaSlot.MAINHAND);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
    }

    // MC 1.9x Compatibility
    @SuppressWarnings("deprecation")
    private static void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!isAir(player.getInventory().getItemInMainHand())) {
            return;
        }
        CheckEquipEvent checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.PICKUP, VanillaSlot.MAINHAND);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
    }

    private static void onHatCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!event.getMessage().toLowerCase().startsWith("/hat")) {
            if (player.isOp()) {
                CheckEquipEvent checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.COMMAND, VanillaSlot.MAINHAND);
                Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
                // 无论如何也不取消普通Command事件
            }
            return;
        }
        if (!isAir(player.getInventory().getItemInMainHand())) {
            return;
        }
        CheckEquipEvent checkEquipEvent = new CheckEquipEvent(player, CheckTrigger.COMMAND_HAT, VanillaSlot.HELMET);
        Bukkit.getServer().getPluginManager().callEvent(checkEquipEvent);
    }
}
