package me.dustin.jex.feature.mod.impl.movement.fly.impl;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;

public class JetpackFly extends FeatureExtension {

    public JetpackFly() {
        super(Fly.Mode.JETPACK, Fly.class);
    }

    @Override
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
	boolean jumping = Wrapper.INSTANCE.getOptions().jumpKey.isPressed();
        if (jumping) {
            Wrapper.INSTANCE.getLocalPlayer().jump();
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
