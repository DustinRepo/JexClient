package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;

public class EventRenderEntity extends Event {

    private Entity entity;

    public EventRenderEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
