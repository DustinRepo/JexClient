package me.dustin.events.api;

import me.dustin.events.EventManager;
import me.dustin.events.core.Event;
import me.dustin.events.exception.MethodNotPrivateException;

public class EventAPI extends EventManager{

    private static EventAPI INSTANCE = new EventAPI();

    private static String version = "1.0.2";

    static {
        System.out.println("Event Manager version " + version + " initiated.");
    }

    public void register(Object obj) {
        try {
            super.register(obj);
        } catch (MethodNotPrivateException e) {
            e.printStackTrace();
        }
    }

    public boolean alreadyRegistered(Object object) {
        return super.alreadyRegistered(object);
    }

    public void unregister(Object obj) {
        super.unregister(obj);
    }

    public void run(Event event) {
        super.run(event);
    }

    public void setPrivateOnly(boolean privateOnly) {
        this.privateOnlyMode = privateOnly;
    }

    public static EventAPI getInstance() {
        return INSTANCE;
    }
}
