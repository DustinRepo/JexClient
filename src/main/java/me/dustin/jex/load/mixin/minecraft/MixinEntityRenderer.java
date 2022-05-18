package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventNametagShouldRender;
import me.dustin.jex.event.render.EventRenderNametags;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
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

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    public void renderLabel(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity instanceof LivingEntity) {
            EventRenderNametags eventRenderNametags = new EventRenderNametags((LivingEntity) entity, matrices, vertexConsumers).run();
            if (eventRenderNametags.isCancelled())
                ci.cancel();
        }
    }

}
