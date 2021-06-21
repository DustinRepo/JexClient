package me.dustin.jex.event.render;

import me.dustin.events.core.Event;

public class EventBufferQuadAlpha extends Event {
    private int alpha;

    public EventBufferQuadAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
