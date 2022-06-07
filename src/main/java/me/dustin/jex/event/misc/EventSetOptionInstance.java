package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.client.option.SimpleOption;

public class EventSetOptionInstance extends Event {

    private final SimpleOption<?> optionInstance;
    private boolean shouldIgnoreCheck;

    public EventSetOptionInstance(SimpleOption<?> optionInstance) {
        this.optionInstance = optionInstance;
    }

    public SimpleOption<?> getOptionInstance() {
        return optionInstance;
    }

    public boolean isShouldIgnoreCheck() {
        return shouldIgnoreCheck;
    }

    public void setShouldIgnoreCheck(boolean shouldIgnoreCheck) {
        this.shouldIgnoreCheck = shouldIgnoreCheck;
    }
}
