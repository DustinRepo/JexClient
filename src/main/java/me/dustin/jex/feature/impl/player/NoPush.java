package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPushAwayFromEntity;
import me.dustin.jex.event.world.EventWaterVelocity;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;

@Feat(name = "NoPush", category = FeatureCategory.PLAYER, description = "Don't let others push you around.")
public class NoPush extends Feature {

    @Op(name = "Mobs")
    public boolean mobs = true;
    @Op(name = "Water")
    public boolean water = true;

    @EventListener(events = {EventWaterVelocity.class, EventPushAwayFromEntity.class})
    private void runMethod(Event event) {
        if (water && event instanceof EventWaterVelocity)
            event.cancel();
        if (mobs && event instanceof EventPushAwayFromEntity)
            event.cancel();
    }
}
