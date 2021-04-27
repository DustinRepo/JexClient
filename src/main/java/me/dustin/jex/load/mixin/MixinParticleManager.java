package me.dustin.jex.load.mixin;

import me.dustin.jex.event.world.EventTickParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Inject(method = "tickParticle", at = @At("HEAD"), cancellable = true)
    public void tickParticle1(Particle particle, CallbackInfo ci) {
        EventTickParticle eventTickParticle = new EventTickParticle(particle).run();
        if (eventTickParticle.isCancelled())
            ci.cancel();
    }

}
