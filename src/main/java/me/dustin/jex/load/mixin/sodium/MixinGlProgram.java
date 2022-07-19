package me.dustin.jex.load.mixin.sodium;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.IntFunction;

@Mixin(GlProgram.class)
public class MixinGlProgram {

    @Inject(method = "bindUniform", at = @At(value = "NEW", target = "java/lang/NullPointerException"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void bindUniform(String name, IntFunction<? extends GlUniform<?>> factory, CallbackInfoReturnable<GlUniform<?>> cir, int index) {
        cir.setReturnValue(factory.apply(index));
    }

}
