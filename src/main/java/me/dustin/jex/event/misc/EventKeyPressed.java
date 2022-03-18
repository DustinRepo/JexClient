package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;

public class EventKeyPressed extends Event {

    private int key;
    private int scancode;
    private PressType type;

    public EventKeyPressed(int key, int scancode, PressType type) {
        super();
        this.key = key;
        this.scancode = scancode;
        this.type = type;
    }

    public int getKey() {
        return this.key;
    }

    public PressType getType() {
        return type;
    }

    public int getScancode() {
        return scancode;
    }

    public enum PressType {
        IN_GAME,
        IN_MENU
    }
}
