package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;

public class EventTeamColor extends Event {

    private int color;
    private final Entity entity;

    public EventTeamColor(int color, Entity entity) {
        this.color = color;
        this.entity = entity;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Entity getEntity() {
        return entity;
    }
}
