package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;

public class EventGetFramerateLimit extends Event {

    private int limit = -1;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
