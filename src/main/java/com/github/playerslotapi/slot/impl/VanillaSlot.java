package com.github.playerslotapi.slot.impl;

import com.github.playerslotapi.slot.AbstractSlot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class VanillaSlot extends AbstractSlot {

    public static final VanillaSlot MAINHAND;
    public static final VanillaSlot OFFHAND;
    public static final VanillaSlot HELMET;
    public static final VanillaSlot CHESTPLATE;
    public static final VanillaSlot LEGGINGS;
    public static final VanillaSlot BOOTS;

    static {
        MAINHAND = new VanillaSlot((player) -> player.getInventory().getItemInMainHand(),
                (player, item) -> player.getInventory().setItemInMainHand(item),"mainhand","hand","main");
        OFFHAND = new VanillaSlot((player) -> player.getInventory().getItemInOffHand(),
                (player, item) -> player.getInventory().setItemInOffHand(item),"offhand","off");
        HELMET = new VanillaSlot((player) -> player.getInventory().getHelmet(),
                (player, item) -> player.getInventory().setHelmet(item),"helmet","head");
        CHESTPLATE = new VanillaSlot((player) -> player.getInventory().getChestplate(),
                (player, item) -> player.getInventory().setChestplate(item),"chestplate","chests","chest");
        LEGGINGS = new VanillaSlot((player) -> player.getInventory().getLeggings(),
                (player, item) -> player.getInventory().setLeggings(item),"leggings","legs","leg");
        BOOTS = new VanillaSlot((player) -> player.getInventory().getBoots(),
                (player, item) -> player.getInventory().setBoots(item),"boots","boot","feet","foot");

    }

    private final Function<Player,ItemStack> getter;
    private final BiConsumer<Player, ItemStack> setter;

    private VanillaSlot(Function<Player,ItemStack> getter, BiConsumer<Player, ItemStack> setter, String ... alias){
        super(alias);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public ItemStack get(Player player) {
        return getter.apply(player);
    }

    @Override
    public void set(Player player, ItemStack item) {
        setter.accept(player,item);
    }
}
