package me.dustin.jex.load.mixin.minecraft;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.event.render.EventRender2DItem;
import me.dustin.jex.event.render.EventRenderItem;
import me.dustin.jex.load.impl.IItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer implements IItemRenderer {

	@Shadow
	@Final
	private TextureManager textureManager;


	@Shadow public abstract BakedModel getModel(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entity, int seed);

	@Shadow public float blitOffset;

	@Shadow public abstract void render(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel);

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void preRenderItem(ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (((EventRenderItem) new EventRenderItem(matrices, stack, renderMode, EventRenderItem.RenderTime.PRE, leftHanded).run()).isCancelled())
			ci.cancel();
	}

	@Inject(method = "render", at = @At("RETURN"), cancellable = true)
	public void postRenderItem(ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
		if (((EventRenderItem) new EventRenderItem(matrices, stack, renderMode, EventRenderItem.RenderTime.POST, leftHanded).run()).isCancelled())
			ci.cancel();
	}

	@Inject(method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("RETURN"))
	public void renderGuiItemOverlay(Font renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		ItemRenderer instance = (ItemRenderer) (Object) this;
		new EventRender2DItem(instance, renderer, stack, x, y).run();
	}

	@Override
	public void renderItemIntoGUI(ItemStack itemStack, float x, float y) {
		renderGuiItemModel(itemStack, x, y, this.getModel(itemStack, (Level) null, null, 0));
	}

	protected void renderGuiItemModel(ItemStack stack, float x, float y, BakedModel model) {
		this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		PoseStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.pushPose();
		matrixStack.translate((double) x, (double) y, (double) (100.0F + this.blitOffset));
		matrixStack.translate(8.0D, 8.0D, 0.0D);
		matrixStack.scale(1.0F, -1.0F, 1.0F);
		matrixStack.scale(16.0F, 16.0F, 16.0F);
		RenderSystem.applyModelViewMatrix();
		PoseStack matrixStack2 = new PoseStack();
		MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
		boolean bl = !model.usesBlockLight();
		if (bl) {
			Lighting.setupForFlatItems();
		}

		this.render(stack, ItemTransforms.TransformType.GUI, false, matrixStack2, immediate, 15728880, OverlayTexture.NO_OVERLAY, model);
		immediate.endBatch();
		RenderSystem.enableDepthTest();
		if (bl) {
			Lighting.setupFor3DItems();
		}

		matrixStack.popPose();
		RenderSystem.applyModelViewMatrix();
	}
}
