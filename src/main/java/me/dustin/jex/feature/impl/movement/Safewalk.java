package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "Safewalk", category = FeatureCategory.MOVEMENT, description = "Prevent yourself from walking off of blocks like you're sneaking")
public class Safewalk extends Feature {

    @EventListener(events = {EventWalkOffBlock.class})
    private void runMethod(EventWalkOffBlock eventWalkOffBlock) {
        eventWalkOffBlock.cancel();
    }

}
