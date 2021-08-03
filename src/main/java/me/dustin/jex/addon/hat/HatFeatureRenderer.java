package me.dustin.jex.addon.hat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

public class HatFeatureRenderer extends FeatureRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {
    public HatFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        this.render(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch, Hat.getHat(entity));
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, PlayerEntity snowGolemEntity, float f, float g, float h, float j, float k, float l, ItemStack itemStack) {
        if (!snowGolemEntity.isInvisible() && Hat.hasHat(snowGolemEntity.getUuidAsString().replace("-", ""))) {
            matrixStack.push();
            ((PlayerEntityModel<?>)this.getContextModel()).getHead().rotate(matrixStack);
            matrixStack.translate(0.0D, -0.34375D, 0.0D);
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
            matrixStack.scale(0.625F, -0.625F, -0.625F);
            MinecraftClient.getInstance().getItemRenderer().renderItem(snowGolemEntity, itemStack, Mode.HEAD, false, matrixStack, vertexConsumerProvider, snowGolemEntity.world, i, LivingEntityRenderer.getOverlay(snowGolemEntity, 0.0F), snowGolemEntity.getId());
            matrixStack.pop();
        }
    }
}
