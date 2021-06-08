package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventPlayerVelocity extends Event {

    private int velocityX, velocityY, velocityZ;

    public EventPlayerVelocity(int velocityX, int velocityY, int velocityZ) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    public int getVelocityZ() {
        return velocityZ;
    }

    public void setVelocityZ(int velocityZ) {
        this.velocityZ = velocityZ;
    }
}
