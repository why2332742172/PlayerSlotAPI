package com.github.playerslotapi.slot;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public abstract class PlayerSlot {

    private final String name;

    private final Set<String> aliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public PlayerSlot(String name, String... aliases) {
        this.name = name;
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public abstract boolean isAsyncSafe();

    public void set(Player player, ItemStack item) {
        set(player, item, result -> {

        });
    }

    public abstract void get(Player player, Consumer<ItemStack> callback);

    public abstract void set(Player player, ItemStack item, Consumer<Boolean> callback);

    public final Set<String> getAliases() {
        return aliases;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlayerSlot && name.equals(((PlayerSlot) o).name);
    }

    @Override
    public String toString() {
        return name;
    }
}
