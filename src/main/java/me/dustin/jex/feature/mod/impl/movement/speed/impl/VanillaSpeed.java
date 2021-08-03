package me.dustin.jex.feature.mod.impl.movement.speed.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;

public class VanillaSpeed extends FeatureExtension {

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
