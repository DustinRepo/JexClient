package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRenderWithShader;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferRenderer.class)
public class MixinBufferRenderer {
    @Inject(method = "drawWithShader", at = @At("HEAD"), cancellable = true)
    private static void drawWithShader(BufferBuilder.BuiltBuffer buffer, CallbackInfo ci) {
        EventRenderWithShader eventRenderWithShader = new EventRenderWithShader(buffer).run();
        if (eventRenderWithShader.isCancelled())
            ci.cancel();
    }
}
