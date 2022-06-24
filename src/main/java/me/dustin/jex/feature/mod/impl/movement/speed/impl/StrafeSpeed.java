package me.dustin.jex.feature.mod.impl.movement.speed.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.PathingHelper;

public class StrafeSpeed extends FeatureExtension {

    public StrafeSpeed() {
        super(Speed.Mode.STRAFE, Speed.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove eventMove) {
            Speed speed = Feature.get(Speed.class);
            if ((BaritoneHelper.INSTANCE.isBaritoneRunning() || PathingHelper.INSTANCE.isPathing()) && !Wrapper.INSTANCE.getLocalPlayer().isOnGround())
                return;
            if (PathingHelper.INSTANCE.isPathing()) {
                eventMove.setX(eventMove.getX() * speed.vanillaSpeedProperty.value());
                eventMove.setZ(eventMove.getZ() * speed.vanillaSpeedProperty.value());
            }else
                PlayerHelper.INSTANCE.setMoveSpeed(eventMove, speed.strafeSpeedProperty.value());
        } else if (event instanceof EventPlayerPackets eventPlayerPackets) {
            Speed speed = Feature.get(Speed.class);
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.setVelocityY(speed.hopAmountProperty.value());
            }
        }
    }

}
