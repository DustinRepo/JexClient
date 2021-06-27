package me.dustin.jex.feature.impl.render.esp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2DNoScale;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.FeatureExtension;
import me.dustin.jex.feature.impl.render.esp.ESP;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.*;

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
                ShaderHelper.INSTANCE.boxOutlineFBO.clear(MinecraftClient.IS_SYSTEM_MAC);
                ShaderHelper.INSTANCE.boxOutlineFBO.beginWrite(false);
                RenderSystem.teardownOverlayColor();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
                Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                    if (ESP.INSTANCE.isValid(entity)) {
                        Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                        Box bb = new Box(vec.x - entity.getWidth() + 0.25, vec.y, vec.z - entity.getWidth() + 0.25, vec.x + entity.getWidth() - 0.25, vec.y + entity.getHeight() + 0.1, vec.z + entity.getWidth() - 0.25);
                        if (entity instanceof ItemEntity)
                            bb = new Box(vec.x - 0.15, vec.y + 0.1f, vec.z - 0.15, vec.x + 0.15, vec.y + 0.5, vec.z + 0.15);
                        float yaw = EntityHelper.INSTANCE.getYaw(entity);

                        eventRender3D.getMatrixStack().translate(vec.x, vec.y, vec.z);
                        eventRender3D.getMatrixStack().multiply(new Quaternion(new Vec3f(0, -1, 0), yaw, true));
                        eventRender3D.getMatrixStack().translate(-vec.x, -vec.y, -vec.z);

                        Matrix4f matrix4f = eventRender3D.getMatrixStack().peek().getModel();
                        Color color1 = ColorHelper.INSTANCE.getColor(ESP.INSTANCE.getColor(entity));
                        float minX = (float)bb.minX;
                        float minY = (float)bb.minY;
                        float minZ = (float)bb.minZ;
                        float maxX = (float)bb.maxX;
                        float maxY = (float)bb.maxY;
                        float maxZ = (float)bb.maxZ;
                        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

                        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

                        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

                        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

                        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

                        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

                        eventRender3D.getMatrixStack().translate(vec.x, vec.y, vec.z);
                        eventRender3D.getMatrixStack().multiply(new Quaternion(new Vec3f(0, 1, 0), yaw, true));
                        eventRender3D.getMatrixStack().translate(-vec.x, -vec.y, -vec.z);
                    }
                });
                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);

                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.enableTexture();
                RenderSystem.resetTextureMatrix();
                RenderSystem.depthMask(false);
                ShaderHelper.INSTANCE.boxOutlineShader.render(Wrapper.INSTANCE.getMinecraft().getTickDelta());
                RenderSystem.enableTexture();
                RenderSystem.depthMask(true);
                Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
            }
        } else if (event instanceof EventRender2DNoScale) {
            if (ShaderHelper.INSTANCE.canDrawFBO()) {
                ShaderHelper.INSTANCE.drawBoxOutlineFBO();
                Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
            }
        }
    }

    @Override
    public void enable() {
        if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
            ShaderHelper.INSTANCE.load();
        super.enable();
    }
}
