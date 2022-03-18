package me.dustin.jex.feature.mod.impl.movement.speed.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.helper.world.PathingHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;

public class VanillaSpeed extends FeatureExtension {

    public VanillaSpeed() {
        super("Vanilla", Speed.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove eventMove) {
            Speed speed = Feature.get(Speed.class);
            if ((BaritoneHelper.INSTANCE.isBaritoneRunning() || PathingHelper.INSTANCE.isPathing()) && !Wrapper.INSTANCE.getLocalPlayer().isOnGround())
                return;
            if (PathingHelper.INSTANCE.isPathing()) {
                eventMove.setX(eventMove.getX() * speed.vanillaSpeed);
                eventMove.setZ(eventMove.getZ() * speed.vanillaSpeed);
            } else
                PlayerHelper.INSTANCE.setMoveSpeed(eventMove, speed.vanillaSpeed);
        }
    }

}
