package me.dustin.jex.addon.hat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HatFeatureRenderer extends RenderLayer<Player, PlayerModel<Player>> {
    public HatFeatureRenderer(RenderLayerParent<Player, PlayerModel<Player>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, Player entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        this.render(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch, Hat.getHat(entity));
    }

    public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, Player snowGolemEntity, float f, float g, float h, float j, float k, float l, ItemStack itemStack) {
        if (!snowGolemEntity.isInvisible() && Hat.hasHat(snowGolemEntity.getStringUUID().replace("-", ""))) {
            matrixStack.pushPose();
            ((PlayerModel<?>)this.getParentModel()).getHead().translateAndRotate(matrixStack);
            matrixStack.translate(0.0D, -0.34375D, 0.0D);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            matrixStack.scale(0.625F, -0.625F, -0.625F);
            Minecraft.getInstance().getItemRenderer().renderStatic(snowGolemEntity, itemStack, TransformType.HEAD, false, matrixStack, vertexConsumerProvider, snowGolemEntity.level, i, LivingEntityRenderer.getOverlayCoords(snowGolemEntity, 0.0F), snowGolemEntity.getId());
            matrixStack.popPose();
        }
    }
}
