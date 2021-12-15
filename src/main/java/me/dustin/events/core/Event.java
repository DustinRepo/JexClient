package me.dustin.events.core;

import me.dustin.events.EventManager;

import java.util.ConcurrentModificationException;

public class Event {

    private boolean cancelled;

    public <T> T run()
    {
        try {
            EventManager.run(this);
        } catch (ConcurrentModificationException ignored){}
        return (T) this;
    }

    public void cancel()
    {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean equals(Class<? extends Event> eventClass)
    {
    	return this.getClass() == eventClass;
    }
    
}
