package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventNametagShouldRender;
import me.dustin.jex.event.render.EventRenderNametags;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public void shouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        EventNametagShouldRender eventNametagShouldRender = new EventNametagShouldRender(entity).run();
        if (eventNametagShouldRender.isCancelled())
            ci.setReturnValue(true);
    }

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    public void renderLabel(Entity entity, Component text, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof LivingEntity) {
            EventRenderNametags eventRenderNametags = new EventRenderNametags((LivingEntity) entity, matrices, vertexConsumers).run();
            if (eventRenderNametags.isCancelled())
                ci.cancel();
        }
    }

}
