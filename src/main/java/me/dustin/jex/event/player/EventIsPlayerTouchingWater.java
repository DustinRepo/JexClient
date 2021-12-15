package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventIsPlayerTouchingWater extends Event {

    private boolean isTouchingWater;

    public EventIsPlayerTouchingWater(boolean isTouchingWater) {
        this.isTouchingWater = isTouchingWater;
    }

    public boolean isTouchingWater() {
        return isTouchingWater;
    }

    public void setTouchingWater(boolean touchingWater) {
        isTouchingWater = touchingWater;
    }
}
