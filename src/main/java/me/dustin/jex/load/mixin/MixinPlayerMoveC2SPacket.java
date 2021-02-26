package me.dustin.jex.load.mixin;

import me.dustin.jex.load.impl.IPlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerMoveC2SPacket.class)
public class MixinPlayerMoveC2SPacket implements IPlayerMoveC2SPacket {
    @Shadow
    protected double x;

    @Shadow
    protected double y;

    @Shadow
    protected double z;

    @Shadow
    protected boolean onGround;

    @Shadow
    protected float yaw;

    @Shadow
    protected float pitch;

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public float getYaw() {
        return this.yaw;
    }

    @Override
    public void setYaw(float z) {
        this.yaw = z;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public void setPitch(float z) {
        this.pitch = z;
    }

    @Override
    public boolean getOnGround() {
        return this.onGround;
    }

    @Override
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
