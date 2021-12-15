package me.dustin.jex.event.player;

import me.dustin.events.core.Event;
import net.minecraft.entity.EntityPose;

public class EventGetPose extends Event {

    private EntityPose pose;

    public EventGetPose(EntityPose pose) {
        this.pose = pose;
    }

    public EntityPose getPose() {
        return pose;
    }

    public void setPose(EntityPose pose) {
        this.pose = pose;
    }
}
