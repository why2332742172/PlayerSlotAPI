package com.github.playerslotapi.slot.impl;

import com.github.playerslotapi.event.SlotUpdateEvent;
import com.github.playerslotapi.event.UpdateTrigger;
import com.github.playerslotapi.slot.PlayerSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class VanillaEquipSlot extends PlayerSlot {

    public static final VanillaEquipSlot MAINHAND;
    public static final VanillaEquipSlot OFFHAND;
    public static final VanillaEquipSlot HELMET;
    public static final VanillaEquipSlot CHESTPLATE;
    public static final VanillaEquipSlot LEGGINGS;
    public static final VanillaEquipSlot BOOTS;
    private static final Map<Integer, VanillaEquipSlot> ID_MAP = new HashMap<>();

    static {
        MAINHAND = new VanillaEquipSlot((player) -> player.getInventory().getItemInMainHand(),
                (player, item) -> player.getInventory().setItemInMainHand(item), "MAINHAND", -1, "mainhand", "hand", "main");
        OFFHAND = new VanillaEquipSlot((player) -> player.getInventory().getItemInOffHand(),
                (player, item) -> player.getInventory().setItemInOffHand(item), "OFFHAND", 45, "offhand", "off");
        HELMET = new VanillaEquipSlot((player) -> player.getInventory().getHelmet(),
                (player, item) -> player.getInventory().setHelmet(item), "HELMET", 5, "helmet", "head");
        CHESTPLATE = new VanillaEquipSlot((player) -> player.getInventory().getChestplate(),
                (player, item) -> player.getInventory().setChestplate(item), "CHESTPLATE", 6, "chestplate", "chests", "chest");
        LEGGINGS = new VanillaEquipSlot((player) -> player.getInventory().getLeggings(),
                (player, item) -> player.getInventory().setLeggings(item), "LEGGINGS", 7, "leggings", "legs", "leg");
        BOOTS = new VanillaEquipSlot((player) -> player.getInventory().getBoots(),
                (player, item) -> player.getInventory().setBoots(item), "BOOTS", 8, "boots", "boot", "feet", "foot");

    }

    private final Function<Player, ItemStack> getter;
    private final BiConsumer<Player, ItemStack> setter;
    private final int id;

    private VanillaEquipSlot(Function<Player, ItemStack> getter, BiConsumer<Player, ItemStack> setter, String name, int id, String... alias) {
        super(name, alias);
        this.getter = getter;
        this.setter = setter;
        this.id = id;
        ID_MAP.put(id, this);
    }

    public static VanillaEquipSlot getById(int id) {
        return ID_MAP.get(id);
    }

    public static VanillaEquipSlot matchType(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        String type = itemStack.getType().name();
        return matchType(type);
    }

    public static VanillaEquipSlot matchType(String type) {
        if (type.endsWith("_HELMET") || type.endsWith("_SKULL") || type.endsWith("_HEAD")) {
            return HELMET;
        } else if (type.endsWith("_CHESTPLATE") || "ELYTRA".equals(type)) {
            return CHESTPLATE;
        } else if (type.endsWith("_LEGGINGS")) {
            return LEGGINGS;
        } else if (type.endsWith("_BOOTS")) {
            return BOOTS;
        } else if ("SHIELD".equals(type)) {
            return OFFHAND;
        } else {
            return null;
        }
    }

    @Override
    public boolean isAsyncSafe() {
        return false;
    }

    @Override
    public void get(Player player, Consumer<ItemStack> callback) {
        callback.accept(getter.apply(player));
    }


    public void setSilently(Player player, ItemStack item) {
        setter.accept(player, item);
    }

    @Override
    public void set(Player player, ItemStack item, Consumer<Boolean> callback) {
        setter.accept(player, item);
        SlotUpdateEvent updateEvent = new SlotUpdateEvent(UpdateTrigger.SET, player, this, null, item);
        updateEvent.setUpdateImmediately();
        Bukkit.getPluginManager().callEvent(updateEvent);
        callback.accept(true);
    }

    public ItemStack get(Player player) {
        return getter.apply(player);
    }

    public int getId() {
        return id;
    }
}
