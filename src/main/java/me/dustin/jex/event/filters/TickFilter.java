package me.dustin.jex.event.filters;

import me.dustin.jex.event.misc.EventTick;

import java.util.function.Predicate;

public class TickFilter implements Predicate<EventTick> {

    private EventTick.Mode mode;

    public TickFilter(EventTick.Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean test(EventTick eventTick) {
        return eventTick.getMode() == this.mode;
    }
}
