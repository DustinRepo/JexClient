package me.dustin.jex.helper.addon.hat;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class JexHatFeatureRenderer extends FeatureRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {
    public JexHatFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntity playerEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        String uuid = playerEntity.getUuidAsString().replace("-", "");
        if (HatHelper.INSTANCE.hasHat(uuid))
            this.render(matrices, vertexConsumers, light, playerEntity, HatHelper.INSTANCE.getHatTexture(uuid), HatHelper.INSTANCE.getType(playerEntity));
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, PlayerEntity playerEntity, Identifier texture, HatHelper.HatType hatType) {
        if (playerEntity.isInvisible())
            return;
        matrixStack.push();
        this.getContextModel().getHead().rotate(matrixStack);
        matrixStack.translate(0.0D, -0.5, 0.0D);
        HatHelper.INSTANCE.renderHat(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(texture)), light, LivingEntityRenderer.getOverlay(playerEntity, 0.0f), hatType);
        matrixStack.pop();
    }
}
