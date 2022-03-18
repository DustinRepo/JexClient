package me.dustin.jex.event.filters;

import me.dustin.jex.event.render.EventRenderToolTip;
import net.minecraft.item.Item;

import java.util.function.Predicate;

public class ToolTipFilter implements Predicate<EventRenderToolTip> {

    private final EventRenderToolTip.Mode mode;
    private final Item[] items;

    public ToolTipFilter(EventRenderToolTip.Mode mode, Item... items) {
        this.mode = mode;
        this.items = items;
    }

    @Override
    public boolean test(EventRenderToolTip eventRenderToolTip) {
        if (items.length > 0) {
            for (Item item : items) {
                if (mode != null)
                    return item == eventRenderToolTip.getItemStack().getItem() && mode == eventRenderToolTip.getMode();
                return item == eventRenderToolTip.getItemStack().getItem();
            }
            return false;
        }
        if (mode != null)
            return eventRenderToolTip.getMode() == mode;
        return true;
    }
}
