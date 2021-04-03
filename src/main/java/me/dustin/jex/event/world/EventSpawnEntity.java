package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;

public class EventSpawnEntity extends Event {

    private Entity entity;

    public EventSpawnEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
