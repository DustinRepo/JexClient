package me.dustin.jex.event.player;

import me.dustin.events.core.Event;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;

public class EventPlayerPackets extends Event {

    private final Mode mode;
    private float yaw;
    private float pitch;
    private boolean onGround;

    public EventPlayerPackets(float yaw, float pitch, boolean onGround) {
        mode = Mode.PRE;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    public EventPlayerPackets() {
        mode = Mode.POST;
    }


    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRotation(RotationVector rotation) {
        this.yaw = rotation.getYaw();
        this.pitch = rotation.getPitch();
        if (Wrapper.INSTANCE.getPlayer() == Freecam.playerEntity) {
            Freecam.playerEntity.setYRot(yaw);
            Freecam.playerEntity.setXRot(pitch);
        }
    }

    public RotationVector getRotation() {
        return new RotationVector(yaw, pitch);
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public Mode getMode() {
        return mode;
    }


    public enum Mode {
        PRE, POST
    }

}
