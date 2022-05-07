package me.dustin.jex.event.misc;

import me.dustin.events.core.Event;
import net.minecraft.client.OptionInstance;

public class EventSetOptionInstance extends Event {

    private final OptionInstance<?> optionInstance;
    private boolean shouldIgnoreCheck;

    public EventSetOptionInstance(OptionInstance<?> optionInstance) {
        this.optionInstance = optionInstance;
    }

    public OptionInstance<?> getOptionInstance() {
        return optionInstance;
    }

    public boolean isShouldIgnoreCheck() {
        return shouldIgnoreCheck;
    }

    public void setShouldIgnoreCheck(boolean shouldIgnoreCheck) {
        this.shouldIgnoreCheck = shouldIgnoreCheck;
    }
}
