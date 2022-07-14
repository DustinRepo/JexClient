package me.dustin.jex.feature.mod.impl.render.esp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2DNoScale;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import java.awt.*;

public class OutlineBox extends FeatureExtension {

	public OutlineBox() {
		super(ESP.Mode.BOX_OUTLINE, ESP.class);
	}

	private int lastWidth, lastHeight;
	private final Framebuffer first = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);
	private final Framebuffer second = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);

	@Override
	public void pass(Event event) {
		if (event instanceof EventRender3D eventRender3D) {
			ShaderProgram shader = ShaderHelper.INSTANCE.getOutlineShader();
			checkResize();
			RenderSystem.depthFunc(519);
			first.clear(MinecraftClient.IS_SYSTEM_MAC);
			first.beginWrite(false);
			RenderSystem.teardownOverlayColor();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1, 1, 1, 1);

			BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS/* QUADS */, VertexFormats.POSITION_COLOR);
			BufferBuilder finalBufferBuilder = bufferBuilder;
			Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
				if (ESP.INSTANCE.isValid(entity)) {
					Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
					Box bb = new Box(vec.x - entity.getWidth() + 0.25, vec.y, vec.z - entity.getWidth() + 0.25, vec.x + entity.getWidth() - 0.25, vec.y + entity.getHeight() + 0.1, vec.z + entity.getWidth() - 0.25);
					if (entity instanceof ItemEntity)
						bb = new Box(vec.x - 0.15, vec.y + 0.1f, vec.z - 0.15, vec.x + 0.15, vec.y + 0.5, vec.z + 0.15);
					float yaw = EntityHelper.INSTANCE.getYaw(entity);

					eventRender3D.getPoseStack().translate(vec.x, vec.y, vec.z);
					eventRender3D.getPoseStack().multiply(new Quaternion(new Vec3f(0, -1, 0), yaw, true));
					eventRender3D.getPoseStack().translate(-vec.x, -vec.y, -vec.z);

					Matrix4f matrix4f = eventRender3D.getPoseStack().peek().getPositionMatrix();
					Color color1 = ColorHelper.INSTANCE.getColor(ESP.INSTANCE.getColor(entity));
					float minX = (float) bb.minX;
					float minY = (float) bb.minY;
					float minZ = (float) bb.minZ;
					float maxX = (float) bb.maxX;
					float maxY = (float) bb.maxY;
					float maxZ = (float) bb.maxZ;
					finalBufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

					finalBufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

					finalBufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

					finalBufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

					finalBufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

					finalBufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
					finalBufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

					eventRender3D.getPoseStack().translate(vec.x, vec.y, vec.z);
					eventRender3D.getPoseStack().multiply(new Quaternion(new Vec3f(0, 1, 0), yaw, true));
					eventRender3D.getPoseStack().translate(-vec.x, -vec.y, -vec.z);
				}
			});
			bufferBuilder.clear();
			BufferRenderer.drawWithShader(bufferBuilder.end());

			RenderSystem.disableBlend();
			RenderSystem.disableDepthTest();
			RenderSystem.enableTexture();
			RenderSystem.resetTextureMatrix();
			RenderSystem.depthMask(false);

			//render shader effect
			this.first.endWrite();
			this.first.beginRead();
			float f = this.second.textureWidth;
			float g = this.second.textureHeight;
			RenderSystem.viewport(0, 0, (int)f, (int)g);
			shader.bind();
			shader.getUniform("Projection").setMatrix(Matrix4x4.copyFromColumnMajor(Matrix4f.projectionMatrix(0.0f, Wrapper.INSTANCE.getMinecraft().getFramebuffer().textureWidth, Wrapper.INSTANCE.getMinecraft().getFramebuffer().textureHeight, 0.0f, 0.1f, 1000.0f)));
			shader.getUniform("Width").setInt(ESP.INSTANCE.lineWidthProperty.value());
			shader.getUniform("Glow").setBoolean(ESP.INSTANCE.glowProperty.value());
			shader.getUniform("GlowIntensity").setFloat(ESP.INSTANCE.glowIntensityProperty.value());
			this.second.clear(MinecraftClient.IS_SYSTEM_MAC);
			this.second.beginWrite(false);
			RenderSystem.depthFunc(519);
			bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
			bufferBuilder.vertex(0.0, 0.0, 500.0).next();
			bufferBuilder.vertex(f, 0.0, 500.0).next();
			bufferBuilder.vertex(f, g, 500.0).next();
			bufferBuilder.vertex(0.0, g, 500.0).next();
			BufferRenderer.drawWithoutShader(bufferBuilder.end());
			RenderSystem.depthFunc(515);
			shader.detach();
			this.second.endWrite();
			this.first.endRead();
			Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
		} else if (event instanceof EventRender2DNoScale) {
			checkResize();
			int width = Wrapper.INSTANCE.getWindow().getFramebufferWidth();
			int height = Wrapper.INSTANCE.getWindow().getFramebufferHeight();
			RenderSystem.enableBlend();
			second.draw(width, height, false);
			Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
		}
	}

	void checkResize() {
		if (lastHeight != Wrapper.INSTANCE.getWindow().getFramebufferHeight() || lastWidth != Wrapper.INSTANCE.getWindow().getFramebufferWidth()) {
			first.resize(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
			second.resize(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false);
		}
		lastWidth = Wrapper.INSTANCE.getWindow().getFramebufferWidth();
		lastHeight = Wrapper.INSTANCE.getWindow().getFramebufferHeight();
	}
}
