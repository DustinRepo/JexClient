package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventVisualCooldown;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;

public class NoVisualCooldown extends Feature {

    public NoVisualCooldown() {
        super(Category.VISUAL);
    }

    @EventPointer
    private final EventListener<EventVisualCooldown> eventVisualCooldownEventListener = new EventListener<>(event -> event.cancel());
}
