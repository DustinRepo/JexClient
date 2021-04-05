package me.dustin.jex.load.mixin;

import me.dustin.jex.load.impl.IEntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public class MixinEntityVelocityUpdateS2CPacket implements IEntityVelocityUpdateS2CPacket {
    @Shadow private int velocityX;

    @Shadow private int velocityY;

    @Shadow private int velocityZ;

    @Override
    public int getVelocityX() {
        return this.velocityX;
    }

    @Override
    public int getVelocityY() {
        return this.velocityY;
    }

    @Override
    public int getVelocityZ() {
        return this.velocityZ;
    }

    @Override
    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    @Override
    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    @Override
    public void setVelocityZ(int velocityZ) {
        this.velocityZ = velocityZ;
    }
}
