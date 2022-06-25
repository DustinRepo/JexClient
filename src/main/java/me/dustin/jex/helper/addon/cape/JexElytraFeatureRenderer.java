package me.dustin.jex.helper.addon.cape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class JexElytraFeatureRenderer extends FeatureRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {
    public JexElytraFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> featureRendererContext, EntityModelLoader loader) {
        super(featureRendererContext);
        this.elytra = new ElytraEntityModel(loader.getModelPart(EntityModelLayers.ELYTRA));
    }
    private static final Identifier FILE = new Identifier("textures/entity/elytra.png");
    private final ElytraEntityModel<PlayerEntity> elytra;

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, PlayerEntity playerEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        String uuid = playerEntity.getUuidAsString().replace("-", "");
        if (playerEntity.isInvisible() || !playerEntity.isPartVisible(PlayerModelPart.CAPE) || !CapeHelper.INSTANCE.hasCape(uuid)) {
            return;
        }
        ItemStack itemStack = ((LivingEntity)playerEntity).getEquippedStack(EquipmentSlot.CHEST);
        if (!itemStack.isOf(Items.ELYTRA)) {
            return;
        }
        Identifier identifier = ((AbstractClientPlayerEntity)playerEntity).canRenderCapeTexture() ? CapeHelper.INSTANCE.getCape(uuid) : FILE;
        matrixStack.push();
        matrixStack.translate(0.0, 0.0, 0.125);
        this.getContextModel().copyStateTo(this.elytra);
        this.elytra.setAngles(playerEntity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumerProvider, RenderLayer.getArmorCutoutNoCull(identifier), false, itemStack.hasGlint());
        this.elytra.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
    }
}

