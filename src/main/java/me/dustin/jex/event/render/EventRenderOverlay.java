package me.dustin.jex.event.render;

import me.dustin.events.core.Event;

public class EventRenderOverlay extends Event {

    private final Overlay overlay;

    public EventRenderOverlay(Overlay overlay) {
        this.overlay = overlay;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public enum Overlay {
        UNDERWATER, LAVA, IN_WALL, FIRE, VIGNETTE, PUMPKIN, PORTAL, COLD, SPYGLASS,
    }
}
