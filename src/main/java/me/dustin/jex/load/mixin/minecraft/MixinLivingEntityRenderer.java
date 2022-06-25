package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventRenderFeature;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/feature/FeatureRenderer.render (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    public void renderFeatureRedirect(FeatureRenderer<Entity, EntityModel<Entity>> instance, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, Entity entity, float o, float n, float g, float l, float k, float m) {
        EventRenderFeature eventRenderFeature = new EventRenderFeature(instance, entity).run();
        if (!eventRenderFeature.isCancelled())
            instance.render(matrixStack, vertexConsumerProvider, i, entity, o, n, g, l, k, m);
    }

}
