package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventGetReachDistance;
import me.dustin.jex.event.player.EventHasExtendedReach;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Stretch Armstrong, but nerfed.")
public class Reach extends Feature {
    @Op(name = "Distance", min = 5, max = 6, inc = 0.05f)
    public float distance = 5.5f;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(String.format("%.1f", distance));
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventGetReachDistance> eventGetReachDistanceEventListener = new EventListener<>(event -> {
        event.setReachDistance(distance);
    });

    @EventPointer
    private final EventListener<EventHasExtendedReach> eventHasExtendedReachEventListener = new EventListener<>(event -> {
        event.setExtendedReach(true);
    });
}
