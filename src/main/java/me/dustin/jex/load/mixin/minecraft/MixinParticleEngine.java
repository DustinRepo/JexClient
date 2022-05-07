package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventTickParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine {

    @Inject(method = "tickParticle", at = @At("HEAD"), cancellable = true)
    public void tickParticle1(Particle particle, CallbackInfo ci) {
        EventTickParticle eventTickParticle = new EventTickParticle(particle).run();
        if (eventTickParticle.isCancelled() && particle.isAlive()) {
            ci.cancel();
            particle.remove();
        }
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    public void addParticle1(Particle particle, CallbackInfo ci) {
        EventTickParticle eventTickParticle = new EventTickParticle(particle).run();
        if (eventTickParticle.isCancelled() && particle.isAlive()) {
            ci.cancel();
            particle.remove();
        }
    }

}
