package me.dustin.jex.event.render;

import me.dustin.events.core.Event;

public class EventIsBlockOpaque extends Event {
    private boolean opaque;

    public EventIsBlockOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    public boolean isOpaque() {
        return opaque;
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }
}
