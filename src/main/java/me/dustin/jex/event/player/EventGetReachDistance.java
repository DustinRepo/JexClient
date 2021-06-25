package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventGetReachDistance extends Event {
    private Float reachDistance;

    public Float getReachDistance() {
        return reachDistance;
    }

    public void setReachDistance(float reachDistance) {
        this.reachDistance = reachDistance;
    }
}
