package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventRenderOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderFire(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.FIRE).run();
        if (eventRenderOverlay.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderUnderWater(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
        EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.UNDERWATER).run();
        if (eventRenderOverlay.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderInWall(MinecraftClient minecraftClient, Sprite sprite, MatrixStack matrixStack, CallbackInfo ci) {
        EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.IN_WALL).run();
        if (eventRenderOverlay.isCancelled())
            ci.cancel();
    }

}
