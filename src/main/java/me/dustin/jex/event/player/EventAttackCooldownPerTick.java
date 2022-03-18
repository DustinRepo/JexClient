package me.dustin.jex.event.player;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;

public class EventAttackCooldownPerTick extends Event {

    private double value = 1;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
