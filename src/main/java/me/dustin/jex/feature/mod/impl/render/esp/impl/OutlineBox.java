package me.dustin.jex.feature.mod.impl.render.esp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2DNoScale;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.awt.*;

public class OutlineBox extends FeatureExtension {

	public OutlineBox() {
		super("Box Outline", ESP.class);
	}

	@Override
	public void pass(Event event) {
		if (event instanceof EventRender3D) {
			EventRender3D eventRender3D = (EventRender3D) event;

			if (ShaderHelper.INSTANCE.canDrawFBO()) {
				RenderSystem.depthFunc(519);
				ShaderHelper.INSTANCE.boxOutlineFBO.clear(Minecraft.ON_OSX);
				ShaderHelper.INSTANCE.boxOutlineFBO.bindWrite(false);
				RenderSystem.teardownOverlayColor();
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.setShaderColor(1, 1, 1, 1);

				BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
				bufferBuilder.begin(VertexFormat.Mode.QUADS/* QUADS */, DefaultVertexFormat.POSITION_COLOR);
				Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
					if (ESP.INSTANCE.isValid(entity)) {
						Vec3 vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
						AABB bb = new AABB(vec.x - entity.getBbWidth() + 0.25, vec.y, vec.z - entity.getBbWidth() + 0.25, vec.x + entity.getBbWidth() - 0.25, vec.y + entity.getBbHeight() + 0.1, vec.z + entity.getBbWidth() - 0.25);
						if (entity instanceof ItemEntity)
							bb = new AABB(vec.x - 0.15, vec.y + 0.1f, vec.z - 0.15, vec.x + 0.15, vec.y + 0.5, vec.z + 0.15);
						float yaw = EntityHelper.INSTANCE.getYaw(entity);

						eventRender3D.getPoseStack().translate(vec.x, vec.y, vec.z);
						eventRender3D.getPoseStack().mulPose(new Quaternion(new Vector3f(0, -1, 0), yaw, true));
						eventRender3D.getPoseStack().translate(-vec.x, -vec.y, -vec.z);

						Matrix4f matrix4f = eventRender3D.getPoseStack().last().pose();
						Color color1 = ColorHelper.INSTANCE.getColor(ESP.INSTANCE.getColor(entity));
						float minX = (float) bb.minX;
						float minY = (float) bb.minY;
						float minZ = (float) bb.minZ;
						float maxX = (float) bb.maxX;
						float maxY = (float) bb.maxY;
						float maxZ = (float) bb.maxZ;
						bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

						bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

						bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

						bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

						bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

						bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
						bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

						eventRender3D.getPoseStack().translate(vec.x, vec.y, vec.z);
						eventRender3D.getPoseStack().mulPose(new Quaternion(new Vector3f(0, 1, 0), yaw, true));
						eventRender3D.getPoseStack().translate(-vec.x, -vec.y, -vec.z);
					}
				});
				bufferBuilder.clear();
				BufferUploader.drawWithShader(bufferBuilder.end());

				RenderSystem.disableBlend();
				RenderSystem.disableDepthTest();
				RenderSystem.enableTexture();
				RenderSystem.resetTextureMatrix();
				RenderSystem.depthMask(false);
				ShaderHelper.INSTANCE.boxOutlineShader.process(Wrapper.INSTANCE.getMinecraft().getFrameTime());
				RenderSystem.enableTexture();
				RenderSystem.depthMask(true);
				Wrapper.INSTANCE.getMinecraft().getMainRenderTarget().bindWrite(true);
			}
		} else if (event instanceof EventRender2DNoScale) {
			if (ShaderHelper.INSTANCE.canDrawFBO()) {
				ShaderHelper.INSTANCE.drawBoxOutlineFBO();
				Wrapper.INSTANCE.getMinecraft().getMainRenderTarget().bindWrite(true);
			}
		}
	}

	@Override
	public void enable() {
		if (Wrapper.INSTANCE.getMinecraft().levelRenderer != null)
			ShaderHelper.INSTANCE.load();
		super.enable();
	}
}
