package me.dustin.jex.load.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.event.render.EventRenderOverlay;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.render.NoFog;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    @Inject(method = {"applyFog"}, at = @At("HEAD"), cancellable = true)
    private static void applyFogModifyDensity(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, CallbackInfo ci) {
        FluidState fluidState = camera.getSubmergedFluidState();
        Entity entity = camera.getFocusedEntity();
        float s;
        if (fluidState.isIn(FluidTags.WATER)) {
            s = 1.0F;
            s = 0.05F;
            if (entity instanceof ClientPlayerEntity) {
                ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity) entity;
                s -= clientPlayerEntity.getUnderwaterVisibility() * clientPlayerEntity.getUnderwaterVisibility() * 0.03F;
                Biome biome = clientPlayerEntity.world.getBiome(clientPlayerEntity.getBlockPos());
                if (biome.getCategory() == Biome.Category.SWAMP) {
                    s += 0.005F;
                }
            }

            EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.UNDERWATER).run();
            if (eventRenderOverlay.isCancelled()) {
                RenderSystem.fogDensity(0);
            } else
                RenderSystem.fogDensity(entity == Wrapper.INSTANCE.getLocalPlayer() && Feature.get(NoFog.class).getState() ? 0 : s);
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
        } else {
            float v;
            if (fluidState.isIn(FluidTags.LAVA)) {
                if (entity instanceof LivingEntity && ((LivingEntity) entity).hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                    s = 0.0F;
                    v = 3.0F;
                } else {
                    s = 0.25F;
                    v = 1.0F;
                }
                EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.LAVA).run();
                if (eventRenderOverlay.isCancelled()) {
                    s = 0;
                    v = 10000;
                }
            } else if (entity instanceof LivingEntity && ((LivingEntity) entity).hasStatusEffect(StatusEffects.BLINDNESS)) {
                int k = ((LivingEntity) entity).getStatusEffect(StatusEffects.BLINDNESS).getDuration();
                float l = MathHelper.lerp(Math.min(1.0F, (float) k / 20.0F), viewDistance, 5.0F);
                if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                    s = 0.0F;
                    v = l * 0.8F;
                } else {
                    s = l * 0.25F;
                    v = l;
                }
            } else if (thickFog) {
                s = viewDistance * 0.05F;
                v = Math.min(viewDistance, 192.0F) * 0.5F;
            } else if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                s = 0.0F;
                v = viewDistance;
            } else {
                s = viewDistance * 0.75F;
                v = viewDistance;
            }
            if (entity == Wrapper.INSTANCE.getLocalPlayer() && Feature.get(NoFog.class).getState()) {
                s = 0;
                v = 10000;
            }
            RenderSystem.fogStart(s);
            RenderSystem.fogEnd(v);
            RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
            RenderSystem.setupNvFogDistance();
        }
        ci.cancel();
    }
}
