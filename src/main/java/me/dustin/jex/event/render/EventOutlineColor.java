package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.entity.Entity;

public class EventOutlineColor extends Event {

    private int color;
    private Entity entity;

    public EventOutlineColor(int color, Entity entity) {
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
