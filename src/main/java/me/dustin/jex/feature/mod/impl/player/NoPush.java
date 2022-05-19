package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPushAwayFromEntity;
import me.dustin.jex.event.world.EventWaterVelocity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

public class NoPush extends Feature {

    @Op(name = "Mobs")
    public boolean mobs = true;
    @Op(name = "Water")
    public boolean water = true;

    public NoPush() {
        super(Category.PLAYER, "Don't let others push you around.");
    }

    @EventPointer
    private final EventListener<EventWaterVelocity> eventWaterVelocityEventListener = new EventListener<>(event -> {
       if (water) event.cancel();
    });

    @EventPointer
    private final EventListener<EventPushAwayFromEntity> eventPushAwayFromEntityEventListener = new EventListener<>(event -> {
       if (mobs) event.cancel();
    });
}
