package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;

public class EventRenderTick extends Event {

    public float timeScale = 0;

    public EventRenderTick(float timeScale)
    {
        this.timeScale = timeScale;
    }

}
