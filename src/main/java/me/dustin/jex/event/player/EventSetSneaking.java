package me.dustin.jex.event.player;


import me.dustin.events.core.Event;

public class EventSetSneaking extends Event {

    private boolean sneaking;

    public EventSetSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }
}
