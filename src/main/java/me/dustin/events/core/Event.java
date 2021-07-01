package me.dustin.events.core;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.api.EventAPI;

import java.util.ConcurrentModificationException;

public class Event {

    private boolean cancelled;

    public <T> T run()
    {
        try {
            EventAPI.getInstance().run(this);
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
