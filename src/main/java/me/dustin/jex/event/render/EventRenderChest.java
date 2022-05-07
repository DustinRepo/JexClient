package me.dustin.jex.event.render;

import me.dustin.events.core.Event;

public class EventRenderChest extends Event {

    private boolean christmas;
    private final Mode mode;

    public EventRenderChest(Mode mode, boolean christmas) {
        this.christmas = christmas;
        this.mode = mode;
    }

    public boolean isChristmas() {
        return christmas;
    }

    public void setChristmas(boolean christmas) {
        this.christmas = christmas;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST
    }
}
