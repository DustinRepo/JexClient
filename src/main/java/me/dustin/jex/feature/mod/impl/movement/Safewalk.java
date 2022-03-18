package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Prevent yourself from walking off of blocks like you're sneaking")
public class Safewalk extends Feature {
    @EventPointer
    private final EventListener<EventWalkOffBlock> eventWalkOffBlockEventListener = new EventListener<>(Event::cancel);
}
