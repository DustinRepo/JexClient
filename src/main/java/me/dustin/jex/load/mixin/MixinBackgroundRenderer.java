package me.dustin.jex.load.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.event.render.EventRenderOverlay;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.NoFog;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
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
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        Entity entity = camera.getFocusedEntity();
        float u;
        if (cameraSubmersionType == CameraSubmersionType.WATER) {
            u = 1.0F;
            u = 0.05F;
            if (entity instanceof ClientPlayerEntity) {
                ClientPlayerEntity clientPlayerEntity = (ClientPlayerEntity)entity;
                u -= clientPlayerEntity.getUnderwaterVisibility() * clientPlayerEntity.getUnderwaterVisibility() * 0.03F;
                Biome biome = clientPlayerEntity.world.getBiome(clientPlayerEntity.getBlockPos());
                if (biome.getCategory() == Biome.Category.SWAMP) {
                    u += 0.005F;
                }
            }
            EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.UNDERWATER).run();
            if (eventRenderOverlay.isCancelled())
                u = 0;
            RenderSystem.setShaderFogStart(u);
            RenderSystem.setShaderFogEnd(10000);
        } else {
            float x;
            if (cameraSubmersionType == CameraSubmersionType.LAVA) {
                if (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
                    u = 0.0F;
                    x = 3.0F;
                } else {
                    u = 0.25F;
                    x = 1.0F;
                }
                EventRenderOverlay eventRenderOverlay = new EventRenderOverlay(EventRenderOverlay.Overlay.LAVA).run();
                if (eventRenderOverlay.isCancelled()) {
                    u = 0;
                    x = 10000;
                }
            } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.BLINDNESS)) {
                int k = ((LivingEntity)entity).getStatusEffect(StatusEffects.BLINDNESS).getDuration();
                float l = MathHelper.lerp(Math.min(1.0F, (float)k / 20.0F), viewDistance, 5.0F);
                if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                    u = 0.0F;
                    x = l * 0.8F;
                } else {
                    u = l * 0.25F;
                    x = l;
                }
            } else if (cameraSubmersionType == CameraSubmersionType.POWDER_SNOW) {
                u = 0.0F;
                x = 2.0F;
            } else if (thickFog) {
                u = viewDistance * 0.05F;
                x = Math.min(viewDistance, 192.0F) * 0.5F;
            } else if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                u = 0.0F;
                x = viewDistance;
            } else {
                u = viewDistance * 0.75F;
                x = viewDistance;
            }
            if (entity == Wrapper.INSTANCE.getLocalPlayer() && Feature.get(NoFog.class).getState()) {
                u = 0;
                x = 10000;
            }
            RenderSystem.setShaderFogStart(u);
            RenderSystem.setShaderFogEnd(x);
        }

        ci.cancel();
    }
}
