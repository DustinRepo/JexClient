package me.dustin.jex.load.mixin;

import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    public void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, Wrapper.INSTANCE.getMinecraft());
        Explosion explosion = new Explosion(Wrapper.INSTANCE.getMinecraft().world, (Entity) null, packet.getX(), packet.getY(), packet.getZ(), packet.getRadius(), packet.getAffectedBlocks());
        explosion.affectWorld(true);
        EventExplosionVelocity eventExplosionVelocity = new EventExplosionVelocity().run();
        if (!eventExplosionVelocity.isCancelled())
            Wrapper.INSTANCE.getLocalPlayer().setVelocity(Wrapper.INSTANCE.getLocalPlayer().getVelocity().add((double) packet.getPlayerVelocityX(), (double) packet.getPlayerVelocityY(), (double) packet.getPlayerVelocityZ()));
        ci.cancel();
    }

}
