package me.dustin.jex.helper.math.vector;

import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class RotationVector {

    private float yaw, pitch;

    public RotationVector(LivingEntity entity) {
        this.yaw = EntityHelper.INSTANCE.getYaw(entity);
        this.pitch = EntityHelper.INSTANCE.getPitch(entity);
    }

    public RotationVector(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void normalize() {
        this.yaw = Mth.wrapDegrees(yaw);
        this.pitch = Mth.wrapDegrees(pitch);
    }

    public void add(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public static RotationVector fromPlayer() {
        return new RotationVector(Wrapper.INSTANCE.getLocalPlayer());
    }
}
