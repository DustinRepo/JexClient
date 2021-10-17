package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventGetReachDistance;
import me.dustin.jex.event.player.EventHasExtendedReach;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Stretch Armstrong, but nerfed.")
public class Reach extends Feature {
    @Op(name = "Distance", min = 5, max = 6, inc = 0.05f)
    public float distance = 5.5f;

    @EventListener(events = {EventGetReachDistance.class, EventHasExtendedReach.class})
    public void run(Event event) {
        if (event instanceof EventGetReachDistance eventGetReachDistance) {
            eventGetReachDistance.setReachDistance(distance);
        }
        if (event instanceof EventHasExtendedReach eventHasExtendedReach) {
            eventHasExtendedReach.setExtendedReach(true);
        }
    }
}
