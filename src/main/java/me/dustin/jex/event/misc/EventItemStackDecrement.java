package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.world.item.ItemStack;

public class EventItemStackDecrement extends Event {
    private final ItemStack itemStack;
    private final int amount;

    public EventItemStackDecrement(ItemStack itemStack, int amount) {
        this.itemStack = itemStack;
        this.amount = amount;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getAmount() {
        return amount;
    }
}
