package me.dustin.jex.feature.mod.impl.movement.fly.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.PathingHelper;

public class NormalFly extends FeatureExtension {
    Fly fly;
    public NormalFly() {
        super("Normal", Fly.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets && eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (fly == null)
                fly = Feature.get(Fly.class);
            boolean jumping = Wrapper.INSTANCE.getOptions().jumpKey.isPressed();
            boolean sneaking = Wrapper.INSTANCE.getOptions().sneakKey.isPressed();


            if (fly.mode.equalsIgnoreCase("Normal")) {
                Wrapper.INSTANCE.getLocalPlayer().airStrafingSpeed = fly.speed;
                if (!PathingHelper.INSTANCE.isPathing() || PathingHelper.INSTANCE.isThinking()) {
                    PlayerHelper.INSTANCE.setVelocityX(0);
                    PlayerHelper.INSTANCE.setVelocityZ(0);
                }
                PlayerHelper.INSTANCE.setVelocityY(0);
                if (!jumping || !sneaking) {
                    if (jumping) {
                        PlayerHelper.INSTANCE.setVelocityY(fly.speed);
                    } else if (sneaking) {
                        PlayerHelper.INSTANCE.setVelocityY(-fly.speed);
                    }
                }
                if (fly.glide && !jumping) {
                    PlayerHelper.INSTANCE.setVelocityY(-fly.glideSpeed);
                }
            }
        }
    }
}
