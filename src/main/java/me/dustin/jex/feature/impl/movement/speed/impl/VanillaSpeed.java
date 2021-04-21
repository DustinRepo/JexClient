package me.dustin.jex.feature.impl.movement.speed.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.extension.FeatureExtension;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.movement.speed.Speed;

public class VanillaSpeed extends FeatureExtension {

    private boolean slowdown = false;

    public VanillaSpeed() {
        super("Vanilla", Speed.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove) {
            if (BaritoneHelper.INSTANCE.isBaritoneRunning() && !Wrapper.INSTANCE.getLocalPlayer().isOnGround())
                return;
            PlayerHelper.INSTANCE.setMoveSpeed((EventMove) event, ((Speed) Feature.get(Speed.class)).vanillaSpeed);
        }
    }

}
