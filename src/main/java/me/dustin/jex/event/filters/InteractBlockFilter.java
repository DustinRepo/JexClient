package me.dustin.jex.event.filters;

import me.dustin.jex.event.world.EventInteractBlock;

import java.util.function.Predicate;

public class InteractBlockFilter implements Predicate<EventInteractBlock> {

    private final EventInteractBlock.Mode mode;

    public InteractBlockFilter(EventInteractBlock.Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean test(EventInteractBlock eventInteractBlock) {
        return eventInteractBlock.getMode() == mode;
    }
}
