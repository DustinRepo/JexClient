package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventSetupFog;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @Inject(method = {"setupFog"}, at = @At("RETURN"))
    private static void applyFogModifyDensity(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog, float f, CallbackInfo ci) {
        new EventSetupFog(camera.getFluidInCamera()).run();
    }
}
