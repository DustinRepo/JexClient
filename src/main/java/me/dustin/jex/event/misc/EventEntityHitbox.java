package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class EventEntityHitbox extends Event {

    private Entity entity;
    private Box box;

    public EventEntityHitbox(Entity entity, Box box) {
        this.entity = entity;
        this.box = box;
    }

    public Entity getEntity() {
        return entity;
    }

    public Box getBox() {
        return box;
    }

    public void setBox(Box box) {
        this.box = box;
    }
}
