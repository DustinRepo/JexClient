package me.dustin.jex.event.player;

import me.dustin.events.core.Event;
import net.minecraft.world.entity.Entity;

public class EventAttackEntity extends Event {

	private Entity entity;

	public EventAttackEntity(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}
}
