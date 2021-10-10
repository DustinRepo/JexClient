package me.dustin.jex.feature.mod.impl.movement.speed.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.event.player.EventPlayerPackets;
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
        if (event instanceof EventMove eventMove) {
            Speed speed = ((Speed) Feature.get(Speed.class));
            if (BaritoneHelper.INSTANCE.isBaritoneRunning() && !Wrapper.INSTANCE.getLocalPlayer().isOnGround())
                return;
            PlayerHelper.INSTANCE.setMoveSpeed(eventMove, speed.vanillaSpeed);
        } else if (event instanceof EventPlayerPackets eventPlayerPackets) {
            Speed speed = ((Speed) Feature.get(Speed.class));
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE && speed.autoHop) {
                if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.setVelocityY(0.42f);
            }
        }
    }

}
