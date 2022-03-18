package me.dustin.jex.event.filters;

import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;

import java.util.function.Predicate;

public class ClickBlockFilter implements Predicate<EventClickBlock> {

    private final EventClickBlock.Mode mode;

    public ClickBlockFilter(EventClickBlock.Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean test(EventClickBlock eventClickBlock) {
        return eventClickBlock.getMode() == mode;
    }
}
