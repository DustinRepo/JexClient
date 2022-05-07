package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;

public class EventTick extends Event {
    private Mode mode;

    public EventTick(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST
    }
}
