package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.item.ItemStack;

public class EventItemStackSetCount extends Event {
    private final Mode mode;
    private final ItemStack itemStack;
    private final int count;

    public EventItemStackSetCount(Mode mode, ItemStack itemStack, int count) {
        this.mode = mode;
        this.itemStack = itemStack;
        this.count = count;
    }

    public Mode getMode() {
        return mode;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getCount() {
        return count;
    }

    public enum Mode {
        PRE, POST
    }
}
