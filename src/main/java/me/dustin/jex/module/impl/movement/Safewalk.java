package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "Safewalk", category = ModCategory.MOVEMENT, description = "Prevent yourself from walking off of blocks like you're sneaking")
public class Safewalk extends Module {

    @EventListener(events = {EventWalkOffBlock.class})
    private void runMethod(EventWalkOffBlock eventWalkOffBlock) {
        eventWalkOffBlock.cancel();
    }

}
