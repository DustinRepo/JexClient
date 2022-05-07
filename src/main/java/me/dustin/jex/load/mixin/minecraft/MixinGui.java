package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRenderCrosshair;
import me.dustin.jex.event.render.EventRenderEffects;
import me.dustin.jex.event.render.EventRenderOverlay;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Shadow @Final private static ResourceLocation PUMPKIN_BLUR_LOCATION;

    @Shadow @Final private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/KeyMapping.isDown()Z"))
    public void draw(PoseStack matrixStack, float float_1, CallbackInfo ci) {
        try {
            new EventRender2D(matrixStack).run();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    public void renderCrosshair(PoseStack matrixStack, CallbackInfo ci) {
        EventRenderCrosshair eventRenderCrosshair = new EventRenderCrosshair(matrixStack).run();
        if (eventRenderCrosshair.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    public void renderStatusEffectOverlay(PoseStack matrixStack, CallbackInfo ci) {
        EventRenderEffects eventRenderEffects = new EventRenderEffects().run();
        if (eventRenderEffects.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPumpkin(ResourceLocation texture, float opacity, CallbackInfo ci) {
        if (texture == PUMPKIN_BLUR_LOCATION) {
            EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.PUMPKIN).run();
            if (eventRenderOverlay.isCancelled())
                ci.cancel();
        }

        if (texture == POWDER_SNOW_OUTLINE_LOCATION) {
            EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.COLD).run();
            if (eventRenderOverlay.isCancelled())
                ci.cancel();
        }
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    public void renderSpyglassOverlay(float scale, CallbackInfo ci) {
        EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.SPYGLASS).run();
        if (eventRenderOverlay.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    public void renderVignetteOverlay(Entity entity, CallbackInfo ci) {
        EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.VIGNETTE).run();
        if (eventRenderOverlay.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPumpkin(float s, CallbackInfo ci) {
        EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.PORTAL).run();
        if (eventRenderOverlay.isCancelled())
            ci.cancel();
    }
}
