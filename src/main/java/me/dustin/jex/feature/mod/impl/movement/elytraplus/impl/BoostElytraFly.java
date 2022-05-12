package me.dustin.jex.feature.mod.impl.movement.elytraplus.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.ElytraPlus;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class BoostElytraFly extends FeatureExtension {
    private ElytraPlus elytraPlus;
    public BoostElytraFly() {
        super("Boost", ElytraPlus.class);
    }

    @Override
    public void pass(Event event) {
        if (elytraPlus == null)
            elytraPlus = Feature.get(ElytraPlus.class);
        if (event instanceof EventMove eventMove) {
            if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                ClientPlayerEntity player = Wrapper.INSTANCE.getLocalPlayer();
                double currentVel = Math.abs(player.getVelocity().x) + Math.abs(player.getVelocity().y) + Math.abs(player.getVelocity().z);
                float radianYaw = (float) Math.toRadians(player.getYaw());
                if (currentVel <= elytraPlus.maxBoost) {
                    if (KeyboardHelper.INSTANCE.isPressed(elytraPlus.boostKey)) {
                        player.addVelocity(MathHelper.sin(radianYaw) * -elytraPlus.boost, 0, MathHelper.cos(radianYaw) * elytraPlus.boost);
                    } else if (KeyboardHelper.INSTANCE.isPressed(elytraPlus.slowKey)) {
                        player.addVelocity(MathHelper.sin(radianYaw) * elytraPlus.boost, 0, MathHelper.cos(radianYaw) * -elytraPlus.boost);
                    }
                }
            }
        }
    }
}
