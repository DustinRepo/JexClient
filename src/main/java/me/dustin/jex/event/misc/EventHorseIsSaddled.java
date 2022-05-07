package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.world.entity.Entity;

public class EventHorseIsSaddled extends Event {

    private boolean isSaddled;
    private final Entity entity;

    public EventHorseIsSaddled(Entity entity) {
        this.entity = entity;
    }

    public boolean isSaddled() {
        return isSaddled;
    }

    public void setSaddled(boolean saddled) {
        isSaddled = saddled;
    }

    public Entity getEntity() {
        return entity;
    }
}
