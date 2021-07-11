package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(name = "Safewalk", category = Feature.Category.MOVEMENT, description = "Prevent yourself from walking off of blocks like you're sneaking")
public class Safewalk extends Feature {

    @EventListener(events = {EventWalkOffBlock.class})
    private void runMethod(EventWalkOffBlock eventWalkOffBlock) {
        eventWalkOffBlock.cancel();
    }

}
