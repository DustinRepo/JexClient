package me.dustin.jex.event.render;

import me.dustin.events.core.Event;

public class EventBlockOutlineColor extends Event {

    private int color;

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }
}
