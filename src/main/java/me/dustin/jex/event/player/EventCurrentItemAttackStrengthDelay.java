package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventCurrentItemAttackStrengthDelay extends Event {

    private double value = 1;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
