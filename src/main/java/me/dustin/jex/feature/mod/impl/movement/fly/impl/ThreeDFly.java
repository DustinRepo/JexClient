package me.dustin.jex.feature.mod.impl.movement.fly.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;

public class ThreeDFly extends FeatureExtension {
    private Fly fly;
    public ThreeDFly() {
        super(Fly.Mode.THREE_D, Fly.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove eventMove) {
            if (fly == null)
                fly = Feature.get(Fly.class);
            double hkmh = fly.hspeedProperty.value() * fly.multipleProperty.value() * 0.01388888888888;
            double vkmh = fly.vspeedProperty.value() * fly.multipleProperty.value() * 0.01388888888888;
            if (!PlayerHelper.INSTANCE.isMoving()) {
                eventMove.setX(0);
                eventMove.setZ(0);
            }
            PlayerHelper.INSTANCE.setMoveSpeed(eventMove, hkmh);
            eventMove.setY(0);
            if (PlayerHelper.INSTANCE.isMoving()) {
                eventMove.setY((vkmh / 50) * -PlayerHelper.INSTANCE.getPitch());
            }
        }
    }
    @Override
    public void disable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            Wrapper.INSTANCE.getLocalPlayer().setVelocity(0, 0, 0);
        }
        super.disable();
    }
}
