package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventVisualCooldown;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "NoVisualCooldown", category = FeatureCategory.VISUAL, description = "Removes the visual effect of your weapon lowering after attacking.")
public class NoVisualCooldown extends Feature {

    @EventListener(events = {EventVisualCooldown.class})
    private void runMethod(EventVisualCooldown eventVisualCooldown) {
        eventVisualCooldown.cancel();
    }
}
