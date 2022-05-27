package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventItemStackDecrement;
import net.minecraft.item.Item;

import java.util.function.Predicate;

public class ItemStackDecrementFilter implements Predicate<EventItemStackDecrement> {

    private final EventItemStackDecrement.Mode mode;
    private final Item[] items;

    @SafeVarargs
    public ItemStackDecrementFilter(EventItemStackDecrement.Mode mode, Item... items) {
        this.mode = mode;
        this.items = items;
    }

    @Override
    public boolean test(EventItemStackDecrement eventItemStackDecrement) {
        if (items.length <= 0) {
            if (mode != null)
                return eventItemStackDecrement.getMode() == mode;
            return true;
        }
        for (Item item : items) {
            if (item == eventItemStackDecrement.getItemStack().getItem()) {
                if (mode != null) {
                    return eventItemStackDecrement.getMode() == mode;
                }
                return true;
            }
        }
        return false;
    }
}
