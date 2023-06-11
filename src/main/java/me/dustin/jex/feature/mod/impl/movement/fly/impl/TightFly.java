package me.dustin.jex.feature.mod.impl.movement.fly.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;

public class TightFly extends FeatureExtension {
    Fly fly;

    public TightFly() {
        super(Fly.Mode.TIGHT, Fly.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove eventMove) {
            if (fly == null)
                fly = Feature.get(Fly.class);
            float hkmh = fly.hspeedProperty.value() * fly.multipleProperty.value() * 0.01388888888888;
            float vkmh = fly.vspeedProperty.value() * fly.multipleProperty.value() * 0.01388888888888;
            if (!PlayerHelper.INSTANCE.isMoving()) {
                eventMove.setX(0);
                eventMove.setZ(0);
            }
            boolean jumping = Wrapper.INSTANCE.getOptions().jumpKey.isPressed();
            boolean sneaking = Wrapper.INSTANCE.getOptions().sneakKey.isPressed();
            eventMove.setY(0);
            PlayerHelper.INSTANCE.setMoveSpeed(eventMove, fly.hkmh);
            if (!jumping || !sneaking) {
                if (jumping) {
                    eventMove.setY(vkmh);
                } else if (sneaking) {
                    eventMove.setY(-vkmh);
                }
            }
            if (fly.glideProperty.value() && !jumping) {
                eventMove.setY(-fly.glideSpeedProperty.value());
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
