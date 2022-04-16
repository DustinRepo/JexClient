package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventApplyFog;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    @Inject(method = {"applyFog"}, at = @At("RETURN"))
    private static void applyFogModifyDensity(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, CallbackInfo ci) {
        new EventApplyFog(camera.getSubmersionType()).run();
    }
}
