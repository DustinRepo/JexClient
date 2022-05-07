package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.MISC, description = "Respawn without having to click anything.")
public class AutoRespawn extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!Wrapper.INSTANCE.getLocalPlayer().isAlive())
            Wrapper.INSTANCE.getLocalPlayer().respawn();
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
