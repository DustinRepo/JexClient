package me.dustin.jex.helper.addon.ears;

import me.dustin.jex.helper.addon.AddonHelper;
import me.dustin.jex.helper.addon.cape.CapeHelper;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class JexEarsFeatureRenderer extends FeatureRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {
    public JexEarsFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntity playerEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        String uuid = playerEntity.getUuidAsString().replace("-", "");
        if (playerEntity.isInvisible() || !EarsHelper.INSTANCE.hasEars(uuid)) {
            return;
        }
        AddonHelper.AddonResponse addonResponse = AddonHelper.INSTANCE.getResponse(uuid);
        render(matrices, vertexConsumers, light, playerEntity, EarsHelper.INSTANCE.getEars(uuid), addonResponse.enchantedears());
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntity playerEntity, Identifier texture, boolean enchanted) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull(texture), false, enchanted);
        matrices.push();
        if (playerEntity.isInSneakingPose())
            matrices.translate(0, 0.25, 0);
        matrices.scale(1.3333334f, 1.3333334f, 1.3333334f);
        this.getContextModel().renderEars(matrices, vertexConsumer, light, LivingEntityRenderer.getOverlay(playerEntity, 0.0f));
        matrices.pop();
    }
}