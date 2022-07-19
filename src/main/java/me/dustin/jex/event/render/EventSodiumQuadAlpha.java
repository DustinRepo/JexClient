package me.dustin.jex.event.render;

import me.dustin.events.core.Event;

public class EventSodiumQuadAlpha extends Event {
    private int alpha;

    public EventSodiumQuadAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
