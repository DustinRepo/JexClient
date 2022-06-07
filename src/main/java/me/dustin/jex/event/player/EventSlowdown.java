package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventSlowdown extends Event {

    private final State state;

    public EventSlowdown(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public enum State {
        USE_ITEM, SOULSAND, COBWEB, BERRY_BUSH, POWDERED_SNOW
    }

}
