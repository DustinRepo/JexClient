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
        super("Hover", ElytraPlus.class);
    }

    @Override
    public void pass(Event event) {
        if (elytraPlus == null)
            elytraPlus = Feature.get(ElytraPlus.class);
        if (event instanceof EventMove eventMove) {
            if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                PlayerHelper.INSTANCE.setMoveSpeed(eventMove, elytraPlus.flySpeed);
                if (eventMove.getY() <= 0)
                    eventMove.setY(Wrapper.INSTANCE.getOptions().jumpKey.isPressed() ? elytraPlus.flySpeed : (Wrapper.INSTANCE.getLocalPlayer().isSneaking() ? -elytraPlus.flySpeed : (elytraPlus.slowGlide ? -0.0001 : 0)));
            }
        }
    }
}