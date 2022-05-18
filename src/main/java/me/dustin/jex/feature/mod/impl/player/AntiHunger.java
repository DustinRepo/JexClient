package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Lose less hunger while sprinting.")
public class AntiHunger extends Feature {
    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().prevY == Wrapper.INSTANCE.getLocalPlayer().getY())
            event.setOnGround(false);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
