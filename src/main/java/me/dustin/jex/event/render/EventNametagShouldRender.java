package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;


public class EventNametagShouldRender extends Event {

    private Entity entity;

    public EventNametagShouldRender(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
