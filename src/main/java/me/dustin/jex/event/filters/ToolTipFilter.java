package me.dustin.jex.event.filters;

import me.dustin.jex.event.render.EventRenderToolTip;

import java.util.function.Predicate;

public class ToolTipFilter implements Predicate<EventRenderToolTip> {

    private final EventRenderToolTip.Mode mode;

    public ToolTipFilter(EventRenderToolTip.Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean test(EventRenderToolTip eventRenderToolTip) {
        return eventRenderToolTip.getMode() == mode;
    }
}
