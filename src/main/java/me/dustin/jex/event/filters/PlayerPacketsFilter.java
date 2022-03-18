package me.dustin.jex.event.filters;

import me.dustin.jex.event.player.EventPlayerPackets;

import java.util.function.Predicate;

public class PlayerPacketsFilter implements Predicate<EventPlayerPackets> {

    private final EventPlayerPackets.Mode mode;

    public PlayerPacketsFilter(EventPlayerPackets.Mode mode) {
        this.mode = mode;
    }

    @Override
    public boolean test(EventPlayerPackets eventPlayerPackets) {
        return eventPlayerPackets.getMode() == mode;
    }
}
