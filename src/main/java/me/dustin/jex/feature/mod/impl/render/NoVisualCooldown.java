package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventVisualCooldown;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Removes the visual effect of your weapon lowering after attacking.")
public class NoVisualCooldown extends Feature {
    @EventPointer
    private final EventListener<EventVisualCooldown> eventVisualCooldownEventListener = new EventListener<>(event -> event.cancel());
}
