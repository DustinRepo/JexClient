package me.dustin.jex.helper.addon.cape;

import me.dustin.jex.helper.addon.AddonHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class JexCapeFeatureRenderer extends FeatureRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {
    public JexCapeFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, PlayerEntity playerEntity, float limbAngle, float g, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        String uuid = playerEntity.getUuidAsString().replace("-", "");
        if (!((AbstractClientPlayerEntity)playerEntity).canRenderCapeTexture() || playerEntity.isInvisible() || !playerEntity.isPartVisible(PlayerModelPart.CAPE) || !CapeHelper.INSTANCE.hasCape(uuid)) {
            return;
        }
        ItemStack itemStack = playerEntity.getEquippedStack(EquipmentSlot.CHEST);
        if (itemStack.isOf(Items.ELYTRA)) {
            return;
        }
        AddonHelper.AddonResponse addonResponse = AddonHelper.INSTANCE.getResponse(uuid);
        try {
        render(matrixStack, vertexConsumerProvider, light, playerEntity, tickDelta, CapeHelper.INSTANCE.getCape(uuid), addonResponse.enchantedcape());
        }
        catch (NullPointerException e) {
        }
    }
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, PlayerEntity playerEntity, float tickDelta, Identifier texture, boolean enchanted) {
        matrixStack.push();
        matrixStack.translate(0.0, 0.0, 0.125);
        double d = MathHelper.lerp(tickDelta, playerEntity.prevCapeX, playerEntity.capeX) - MathHelper.lerp(tickDelta, playerEntity.prevX, playerEntity.getX());
        double e = MathHelper.lerp(tickDelta, playerEntity.prevCapeY, playerEntity.capeY) - MathHelper.lerp(tickDelta, playerEntity.prevY, playerEntity.getY());
        double m = MathHelper.lerp(tickDelta, playerEntity.prevCapeZ, playerEntity.capeZ) - MathHelper.lerp(tickDelta, playerEntity.prevZ, playerEntity.getZ());
        float n = playerEntity.prevBodyYaw + (playerEntity.bodyYaw - playerEntity.prevBodyYaw);
        double o = MathHelper.sin(n * ((float)Math.PI / 180));
        double p = -MathHelper.cos(n * ((float)Math.PI / 180));
        float q = (float)e * 10.0f;
        q = MathHelper.clamp(q, -6.0f, 32.0f);
        float r = (float)(d * o + m * p) * 100.0f;
        r = MathHelper.clamp(r, 0.0f, 150.0f);
        float s = (float)(d * p - m * o) * 100.0f;
        s = MathHelper.clamp(s, -20.0f, 20.0f);
        if (r < 0.0f) {
            r = 0.0f;
        }
        float t = MathHelper.lerp(tickDelta, playerEntity.prevStrideDistance, playerEntity.strideDistance);
        q += MathHelper.sin(MathHelper.lerp(tickDelta, playerEntity.prevHorizontalSpeed, playerEntity.horizontalSpeed) * 6.0f) * 32.0f * t;
        if (playerEntity.isInSneakingPose()) {
            q += 25.0f;
        }
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(6.0f + r / 2.0f + q));
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(s / 2.0f));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - s / 2.0f));
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(texture), false, enchanted);
        this.getContextModel().renderCape(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        matrixStack.pop();
    }
}

