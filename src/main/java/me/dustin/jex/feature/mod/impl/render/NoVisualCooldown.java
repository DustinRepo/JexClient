package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventVisualCooldown;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Removes the visual effect of your weapon lowering after attacking.")
public class NoVisualCooldown extends Feature {

    @EventListener(events = {EventVisualCooldown.class})
    private void runMethod(EventVisualCooldown eventVisualCooldown) {
        eventVisualCooldown.cancel();
    }
}
