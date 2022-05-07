package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventPlayerUpdates extends Event {

    private final Mode mode;

    public EventPlayerUpdates(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST
    }

}
