package me.dustin.jex.event.chat;

import me.dustin.events.core.Event;

public class EventSendMessage extends Event {

    private String message;

    public EventSendMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
