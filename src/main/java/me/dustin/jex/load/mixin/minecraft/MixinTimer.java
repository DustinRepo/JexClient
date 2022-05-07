package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.misc.EventRenderTick;
import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Timer.class)
public class MixinTimer {

    @Mutable
    @Shadow @Final private float msPerTick;

    @Inject(method = "advanceTime", at = @At("HEAD"))
    public void beingRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.msPerTick = ((EventRenderTick)new EventRenderTick(this.msPerTick).run()).timeScale;
    }
}
