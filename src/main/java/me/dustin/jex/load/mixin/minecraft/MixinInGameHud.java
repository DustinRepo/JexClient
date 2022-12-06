package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRenderCrosshair;
import me.dustin.jex.event.render.EventRenderEffects;
import me.dustin.jex.event.render.EventRenderBossBar;
import net.minecraft.client.gui.hud.ClientBossBar;
import me.dustin.jex.event.render.EventRenderOverlay;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Shadow @Final private static Identifier PUMPKIN_BLUR;

    @Shadow @Final private static Identifier POWDER_SNOW_OUTLINE;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    public void draw(MatrixStack matrixStack, float float_1, CallbackInfo ci) {
        try {
            new EventRender2D(matrixStack).run();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
     @Inject(method = "renderClientBossBar", at = @At("HEAD"), cancellable = true)
    public void renderBossbar(ClientBossBar bossbar, CallbackInfo ci) {
        EventRenderBossBar eventRenderBossbar = new EventRenderBossBar(bossbar).run();
        if (eventRenderBossbar.isCancelled())
            ci.cancel();
    }

    
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    public void renderCrosshair(MatrixStack matrixStack, CallbackInfo ci) {
        EventRenderCrosshair eventRenderCrosshair = new EventRenderCrosshair(matrixStack).run();
        if (eventRenderCrosshair.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    public void renderStatusEffectOverlay(MatrixStack matrixStack, CallbackInfo ci) {
        EventRenderEffects eventRenderEffects = new EventRenderEffects().run();
        if (eventRenderEffects.isCancelled())
            ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPumpkin(Identifier texture, float opacity, CallbackInfo ci) {
        if (texture == PUMPKIN_BLUR) {
            EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.PUMPKIN).run();
            if (eventRenderOverlay.isCancelled())
                ci.cancel();
        }

        if (texture == POWDER_SNOW_OUTLINE) {
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

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
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
