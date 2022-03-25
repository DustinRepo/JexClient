package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.client.option.SimpleOption;

public class EventSetSimpleOption extends Event {

    private final SimpleOption<?> simpleOption;
    private boolean shouldIgnoreCheck;

    public EventSetSimpleOption(SimpleOption<?> simpleOption) {
        this.simpleOption = simpleOption;
    }

    public SimpleOption<?> getSimpleOption() {
        return simpleOption;
    }

    public boolean isShouldIgnoreCheck() {
        return shouldIgnoreCheck;
    }

    public void setShouldIgnoreCheck(boolean shouldIgnoreCheck) {
        this.shouldIgnoreCheck = shouldIgnoreCheck;
    }
}
