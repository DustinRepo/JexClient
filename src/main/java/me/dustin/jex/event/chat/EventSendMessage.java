package me.dustin.jex.event.chat;

import me.dustin.events.core.Event;

public class EventSendMessage extends Event {

    private String message;
    private final boolean preview;

    public EventSendMessage(String message, boolean preview) {
        this.message = message;
        this.preview = preview;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPreview() {
        return preview;
    }
}
