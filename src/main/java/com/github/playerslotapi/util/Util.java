package com.github.playerslotapi.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;

public class Util {
    public static boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<E>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }
}
