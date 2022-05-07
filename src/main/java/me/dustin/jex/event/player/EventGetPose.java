package me.dustin.jex.event.player;

import me.dustin.events.core.Event;
import net.minecraft.world.entity.Pose;

public class EventGetPose extends Event {

    private Pose pose;

    public EventGetPose(Pose pose) {
        this.pose = pose;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }
}
