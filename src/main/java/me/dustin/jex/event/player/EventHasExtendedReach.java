package me.dustin.jex.event.player;

import me.dustin.events.core.Event;

public class EventHasExtendedReach extends Event {
    private Boolean extendedReach;

    public Boolean isExtendedReach() {
        return extendedReach;
    }

    public void setExtendedReach(boolean hasExtendedReach) {
        this.extendedReach = hasExtendedReach;
    }
}
