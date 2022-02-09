package com.github.playerslotapi.slot;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractSlot {
    private final Set<String> aliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public AbstractSlot(String ... aliases){
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public abstract ItemStack get(Player player);

    public abstract void set(Player player, ItemStack item);

    public final Set<String> getAliases(){
        return aliases;
    }
}
