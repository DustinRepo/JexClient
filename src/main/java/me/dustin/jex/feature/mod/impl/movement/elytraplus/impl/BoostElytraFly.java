package me.dustin.jex.feature.mod.impl.movement.elytraplus.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.ElytraPlus;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class BoostElytraFly extends FeatureExtension {
    private ElytraPlus elytraPlus;
    public BoostElytraFly() {
        super(ElytraPlus.Mode.BOOST, ElytraPlus.class);
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
                if (currentVel <= elytraPlus.maxBoostProperty.value()) {
                    if (KeyboardHelper.INSTANCE.isPressed(elytraPlus.boostKeyProperty.value())) {
                        player.addVelocity(MathHelper.sin(radianYaw) * -elytraPlus.boostProperty.value(), 0, MathHelper.cos(radianYaw) * elytraPlus.boostProperty.value());
                    } else if (KeyboardHelper.INSTANCE.isPressed(elytraPlus.slowKeyProperty.value())) {
                        player.addVelocity(MathHelper.sin(radianYaw) * elytraPlus.boostProperty.value(), 0, MathHelper.cos(radianYaw) * -elytraPlus.boostProperty.value());
                    }
                }
            }
        }
    }
}
