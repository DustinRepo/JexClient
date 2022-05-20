package me.dustin.jex.event.chat;

import me.dustin.events.core.Event;

public class EventShouldPreviewChat extends Event {

    private boolean enabled;

    public EventShouldPreviewChat(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
