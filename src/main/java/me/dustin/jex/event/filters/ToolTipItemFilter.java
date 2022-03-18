package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventGetToolTipFromItem;
import me.dustin.jex.event.misc.EventSetScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class ToolTipItemFilter implements Predicate<EventGetToolTipFromItem> {

    private final Item[] items;

    @SafeVarargs
    public ToolTipItemFilter(Item... items) {
        this.items = items;
    }

    @Override
    public boolean test(EventGetToolTipFromItem eventGetToolTipFromItem) {
        if (items.length <= 0)
            return true;
        for (Item item : items) {
            if (eventGetToolTipFromItem.getItemStack() == null)
                return item == null;
            if (item == eventGetToolTipFromItem.getItemStack().getItem()) {
                return true;
            }
        }
        return false;
    }
}
