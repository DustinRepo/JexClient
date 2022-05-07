package me.dustin.jex.feature.mod.impl.movement.fly.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;

public class TightFly extends FeatureExtension {
    Fly fly;

    public TightFly() {
        super("Tight", Fly.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove eventMove) {
            if (fly == null)
                fly = Feature.get(Fly.class);
            if (!PlayerHelper.INSTANCE.isMoving()) {
                eventMove.setX(0);
                eventMove.setZ(0);
            }
            boolean jumping = Wrapper.INSTANCE.getOptions().keyJump.isDown();
            boolean sneaking = Wrapper.INSTANCE.getOptions().keyShift.isDown();
            eventMove.setY(0);
            PlayerHelper.INSTANCE.setMoveSpeed(eventMove, fly.speed);
            if (!jumping || !sneaking) {
                if (jumping) {
                    eventMove.setY(fly.speed);
                } else if (sneaking) {
                    eventMove.setY(-fly.speed);
                }
            }
            if (fly.glide && !jumping) {
                eventMove.setY(-fly.glideSpeed);
            }
        }
    }

    @Override
    public void disable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(0, 0, 0);
        }
        super.disable();
    }
}
