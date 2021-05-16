package me.dustin.jex.load.mixin;

import me.dustin.jex.event.misc.EventServerTurn;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    private float yaw, pitch;
    private EventServerTurn eventServerTurn;

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    public void onPlayerPositionLook1(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            eventServerTurn = new EventServerTurn().run();
            yaw = PlayerHelper.INSTANCE.getYaw();
            pitch = PlayerHelper.INSTANCE.getPitch();
        }

    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    public void onPlayerPositionLook2(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        if (eventServerTurn != null && eventServerTurn.isCancelled()) {
            PlayerHelper.INSTANCE.setYaw(yaw);
            PlayerHelper.INSTANCE.setPitch(pitch);
        }
        eventServerTurn = null;
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    public void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, Wrapper.INSTANCE.getMinecraft());
        Explosion explosion = new Explosion(Wrapper.INSTANCE.getMinecraft().world, (Entity) null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks());
        explosion.affectWorld(true);
        EventExplosionVelocity eventExplosionVelocity = new EventExplosionVelocity().run();
        if (!eventExplosionVelocity.isCancelled())
            Wrapper.INSTANCE.getLocalPlayer().setVelocity(Wrapper.INSTANCE.getLocalPlayer().getVelocity().add((double) packet.getPlayerVelocityX() * eventExplosionVelocity.getMultX(), (double) packet.getPlayerVelocityY() * eventExplosionVelocity.getMultY(), (double) packet.getPlayerVelocityZ() * eventExplosionVelocity.getMultZ()));
        ci.cancel();
    }

}
