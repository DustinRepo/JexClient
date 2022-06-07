package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventSetSprint extends Event {

	private boolean sprint;

	public EventSetSprint(boolean sprint) {
		this.sprint = sprint;
	}

	public boolean isSprint() {
		return sprint;
	}

	public void setSprint(boolean sprint) {
		this.sprint = sprint;
	}
}
