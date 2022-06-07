package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventExplosionVelocity extends Event {

	private float multX;
	private float multY;
	private float multZ;

	public float getMultX() {
		return multX;
	}

	public float getMultY() {
		return multY;
	}

	public float getMultZ() {
		return multZ;
	}

	public void setMultX(float multX) {
		this.multX = multX;
	}

	public void setMultY(float multY) {
		this.multY = multY;
	}

	public void setMultZ(float multZ) {
		this.multZ = multZ;
	}
}
