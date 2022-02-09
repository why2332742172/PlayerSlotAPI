package com.github.playerslotapi.slot;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractPlayerSlot {
    private final Set<String> aliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public AbstractPlayerSlot(String ... aliases){
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public abstract ItemStack get();

    public abstract void set();

    public final Set<String> getAliases(){
        return aliases;
    }
}
