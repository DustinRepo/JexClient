package me.dustin.jex.load.mixin;

import me.dustin.jex.load.impl.IPlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerPositionLookS2CPacket.class)
public abstract class MixinPlayerPositionLookS2CPacket implements IPlayerPositionLookS2CPacket {


    @Shadow
    private float pitch;

    @Shadow
    private float yaw;

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
