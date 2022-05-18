package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Jump while on edge of block.")
public class Parkour extends Feature {
    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) > 0.5f && PlayerHelper.INSTANCE.isMoving()) {
            Wrapper.INSTANCE.getLocalPlayer().jump();
            Wrapper.INSTANCE.getLocalPlayer().getVelocity().multiply(1.2f, 1, 1.2f);
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
