package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.util.Identifier;

public class EventPlaySound extends Event {

    private final Mode mode;
    private final Identifier identifier;

    public EventPlaySound(Mode mode, Identifier identifier) {
        this.mode = mode;
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Mode getMode() {
        return mode;
    }

    public enum Mode {
        PRE, POST
    }
}
