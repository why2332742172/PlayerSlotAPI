package com.github.playerslotapi.slot.impl;

import com.github.playerslotapi.slot.AbstractSlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class VanillaSlot extends AbstractSlot {

    public static final VanillaSlot MAINHAND;
    public static final VanillaSlot OFFHAND;
    public static final VanillaSlot HELMET;
    public static final VanillaSlot CHESTPLATE;
    public static final VanillaSlot LEGGINGS;
    public static final VanillaSlot BOOTS;
    private static final Map<Integer, VanillaSlot> ID_MAP = new HashMap<>();

    static {
        MAINHAND = new VanillaSlot((player) -> player.getInventory().getItemInMainHand(),
                (player, item) -> player.getInventory().setItemInMainHand(item), "MAINHAND", -1, "mainhand", "hand", "main");
        OFFHAND = new VanillaSlot((player) -> player.getInventory().getItemInOffHand(),
                (player, item) -> player.getInventory().setItemInOffHand(item), "OFFHAND", 45, "offhand", "off");
        HELMET = new VanillaSlot((player) -> player.getInventory().getHelmet(),
                (player, item) -> player.getInventory().setHelmet(item), "HELMET", 5, "helmet", "head");
        CHESTPLATE = new VanillaSlot((player) -> player.getInventory().getChestplate(),
                (player, item) -> player.getInventory().setChestplate(item), "CHESTPLATE", 6, "chestplate", "chests", "chest");
        LEGGINGS = new VanillaSlot((player) -> player.getInventory().getLeggings(),
                (player, item) -> player.getInventory().setLeggings(item), "LEGGINGS", 7, "leggings", "legs", "leg");
        BOOTS = new VanillaSlot((player) -> player.getInventory().getBoots(),
                (player, item) -> player.getInventory().setBoots(item), "BOOTS", 8, "boots", "boot", "feet", "foot");

    }

    private final Function<Player, ItemStack> getter;
    private final BiConsumer<Player, ItemStack> setter;
    private final int id;

    private VanillaSlot(Function<Player, ItemStack> getter, BiConsumer<Player, ItemStack> setter, String name, int id, String... alias) {
        super(name, alias);
        this.getter = getter;
        this.setter = setter;
        this.id = id;
        ID_MAP.put(id, this);
    }

    public static VanillaSlot getById(int id) {
        return ID_MAP.get(id);
    }

    private static boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static VanillaSlot matchType(final ItemStack itemStack) {
        if (isAir(itemStack)) {
            return null;
        }
        String type = itemStack.getType().name();
        return matchType(type);
    }

    public static VanillaSlot matchType(String type) {
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
    public ItemStack get(Player player) {
        return getter.apply(player);
    }

    @Override
    public void set(Player player, ItemStack item) {
        setter.accept(player, item);
    }

    public int getId() {
        return id;
    }
}
