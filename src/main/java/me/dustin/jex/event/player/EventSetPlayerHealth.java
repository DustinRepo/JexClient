package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventSetPlayerHealth extends Event {

    private float health;

    public EventSetPlayerHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }
}
