package me.dustin.jex.feature.mod.impl.movement.elytraplus.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.ElytraPlus;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.world.phys.Vec3;

public class AlwaysBoostElytraFly extends FeatureExtension {
    public AlwaysBoostElytraFly() {
        super("AlwaysBoost", ElytraPlus.class);
    }

    @Override
    public void pass(Event event) {
        if (event instanceof EventMove eventMove) {
            if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying()) {
                Vec3 vec3d_1 = Wrapper.INSTANCE.getLocalPlayer().getLookAngle();
                Vec3 vec3d_2 = Wrapper.INSTANCE.getLocalPlayer().getDeltaMovement();
                Wrapper.INSTANCE.getLocalPlayer().setDeltaMovement(vec3d_2.add(vec3d_1.x * 0.1D + (vec3d_1.x * 1.5D - vec3d_2.x) * 0.5D, vec3d_1.y * 0.1D + (vec3d_1.y * 1.5D - vec3d_2.y) * 0.5D, vec3d_1.z * 0.1D + (vec3d_1.z * 1.5D - vec3d_2.z) * 0.5D));
            }
        }
    }
}

