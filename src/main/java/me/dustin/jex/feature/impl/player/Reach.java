package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventGetReachDistance;
import me.dustin.jex.event.player.EventHasExtendedReach;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "Reach", category = FeatureCategory.PLAYER, description = "Stretch Armstrong, but nerfed.")
public class Reach extends Feature {
    @EventListener(events = {EventGetReachDistance.class, EventHasExtendedReach.class})
    public void run(Event event) {
        if (event instanceof EventGetReachDistance eventGetReachDistance) {
            eventGetReachDistance.setReachDistance(5.5F);
        }
        if (event instanceof EventHasExtendedReach eventHasExtendedReach) {
            eventHasExtendedReach.setExtendedReach(true);
        }
    }
}
