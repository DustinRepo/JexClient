package me.dustin.jex.feature.mod.impl.movement.elytraplus.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.ElytraPlus;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;

public class HoverElytraFly extends FeatureExtension {
    private ElytraPlus elytraPlus;
    public HoverElytraFly() {
        super(ElytraPlus.Mode.HOVER, ElytraPlus.class);
    }

    @Override
    public void pass(Event event) {
        if (elytraPlus == null)
            elytraPlus = Feature.get(ElytraPlus.class);
        if (event instanceof EventMove eventMove) {
            if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                PlayerHelper.INSTANCE.setMoveSpeed(eventMove, elytraPlus.flySpeedProperty.value());
                if (eventMove.getY() <= 0)
                    eventMove.setY(Wrapper.INSTANCE.getOptions().jumpKey.isPressed() ? elytraPlus.flySpeedProperty.value() : (Wrapper.INSTANCE.getLocalPlayer().isInSneakingPose() ? -elytraPlus.flySpeedProperty.value() : (elytraPlus.slowGlideProperty.value() ? -0.0001 : 0)));
            }
        }
    }
}