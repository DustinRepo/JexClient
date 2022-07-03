package me.dustin.jex.load.mixin.minecraft;

import me.dustin.jex.event.render.EventLivingEntityCallRender;
import me.dustin.jex.event.render.EventRenderFeature;
import me.dustin.jex.event.render.EventShouldFlipUpsideDown;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/feature/FeatureRenderer.render (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    public void renderFeatureRedirect(FeatureRenderer<Entity, EntityModel<Entity>> instance, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, Entity entity, float o, float n, float g, float l, float k, float m) {
        EventRenderFeature eventRenderFeature = new EventRenderFeature(instance, entity).run();
        if (!eventRenderFeature.isCancelled())
            instance.render(matrixStack, vertexConsumerProvider, i, entity, o, n, g, l, k, m);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/model/EntityModel.render (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    public void renderRedir(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        new EventLivingEntityCallRender(livingEntity).run();
    }
    @Inject(method = "shouldFlipUpsideDown", at = @At("RETURN"), cancellable = true)
    private static void shouldFlip(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        EventShouldFlipUpsideDown eventShouldFlipUpsideDown = new EventShouldFlipUpsideDown(entity, cir.getReturnValue()).run();
        cir.setReturnValue(eventShouldFlipUpsideDown.isFlip());
    }
}
