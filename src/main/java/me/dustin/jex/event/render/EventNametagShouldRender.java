package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.world.entity.Entity;


public class EventNametagShouldRender extends Event {

    private final Entity entity;

    public EventNametagShouldRender(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
