package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class EventEntityHitbox extends Event {

    private final Entity entity;
    private AABB box;

    public EventEntityHitbox(Entity entity, AABB box) {
        this.entity = entity;
        this.box = box;
    }

    public Entity getEntity() {
        return entity;
    }

    public AABB getBox() {
        return box;
    }

    public void setBox(AABB box) {
        this.box = box;
    }
}
