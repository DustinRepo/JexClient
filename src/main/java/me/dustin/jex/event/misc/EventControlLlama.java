package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;

public class EventControlLlama extends Event {

    private boolean control;

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }
}
