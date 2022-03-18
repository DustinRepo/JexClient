package me.dustin.jex.event.player;

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;

public class EventStep extends Event {
    private Entity entity;
    private Mode mode;
    private double stepHeight;

    public EventStep(Entity entity, Mode mode, double stepHeight) {
        this.entity = entity;
        this.mode = mode;
        this.stepHeight = stepHeight;
    }

    public Entity getEntity() {
        return entity;
    }

    public Mode getMode() {
        return mode;
    }

    public double getStepHeight() {
        return stepHeight;
    }

    public enum Mode {
        PRE, MID, END, POST
    }
}
