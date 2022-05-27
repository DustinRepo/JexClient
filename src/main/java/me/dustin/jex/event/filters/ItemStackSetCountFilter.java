package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventItemStackSetCount;
import net.minecraft.item.Item;

import java.util.function.Predicate;

public class ItemStackSetCountFilter implements Predicate<EventItemStackSetCount> {

    private final EventItemStackSetCount.Mode mode;
    private final Item[] items;

    @SafeVarargs
    public ItemStackSetCountFilter(EventItemStackSetCount.Mode mode, Item... items) {
        this.mode = mode;
        this.items = items;
    }

    @Override
    public boolean test(EventItemStackSetCount EventItemStackSetCount) {
        if (items.length <= 0) {
            if (mode != null)
                return EventItemStackSetCount.getMode() == mode;
            return true;
        }
        for (Item item : items) {
            if (item == EventItemStackSetCount.getItemStack().getItem()) {
                if (mode != null) {
                    return EventItemStackSetCount.getMode() == mode;
                }
                return true;
            }
        }
        return false;
    }
}
