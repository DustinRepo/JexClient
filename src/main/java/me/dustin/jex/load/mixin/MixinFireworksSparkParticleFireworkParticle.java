package me.dustin.jex.load.mixin;

import me.dustin.jex.event.world.EventRenderFirework;
import net.minecraft.client.particle.FireworksSparkParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworksSparkParticle.FireworkParticle.class)
public class MixinFireworksSparkParticleFireworkParticle {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick1(CallbackInfo ci) {
        EventRenderFirework eventRenderFirework = new EventRenderFirework().run();
        if (eventRenderFirework.isCancelled())
            ci.cancel();
    }

}
