package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.world.EventWeatherGradient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow protected float rainGradient;

    @Shadow protected float rainGradientPrev;

    @Shadow protected float thunderGradient;

    @Shadow protected float thunderGradientPrev;

    @Shadow public abstract float getRainGradient(float delta);

    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    public void getRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        EventWeatherGradient eventWeatherGradient = new EventWeatherGradient(MathHelper.lerp(delta, this.rainGradientPrev, this.rainGradient)).run();
        if (eventWeatherGradient.isCancelled())
            cir.setReturnValue(eventWeatherGradient.getWeatherGradient());
    }

    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
    public void getThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {
        EventWeatherGradient eventWeatherGradient = new EventWeatherGradient(MathHelper.lerp(delta, this.thunderGradientPrev, this.thunderGradient) * this.getRainGradient(delta)).run();
        if (eventWeatherGradient.isCancelled())
            cir.setReturnValue(eventWeatherGradient.getWeatherGradient());
    }

}
