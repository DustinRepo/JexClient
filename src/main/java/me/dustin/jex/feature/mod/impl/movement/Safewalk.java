package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;

public class Safewalk extends Feature {

    public Safewalk() {
        super(Category.MOVEMENT, "Prevent yourself from walking off of blocks like you're sneaking");
    }

    @EventPointer
    private final EventListener<EventWalkOffBlock> eventWalkOffBlockEventListener = new EventListener<>(event -> event.cancel());
}
