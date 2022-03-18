package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;

public class EventHasOutline extends Event {

    private Entity entity;
    private boolean outline;

    public EventHasOutline(Entity entity, boolean outline) {
        this.entity = entity;
        this.outline = outline;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isOutline() {
        return outline;
    }

    public void setOutline(boolean outline) {
        this.outline = outline;
    }
}
