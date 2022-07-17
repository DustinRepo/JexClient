package me.dustin.jex.feature.mod.impl.render.storageesp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.feature.mod.impl.render.storageesp.StorageESP;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IWorldRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import java.awt.*;

public class OutlineStorageESP extends FeatureExtension {
    private StorageESP storageESP;
    public OutlineStorageESP() {
        super(StorageESP.Mode.SHADER, StorageESP.class);
    }

    private final Framebuffer first = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);
    private final Framebuffer second = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);
    private int lastWidth, lastHeight;

    @Override
    public void pass(Event event) {
        if (storageESP == null)
            storageESP = Feature.get(StorageESP.class);
        IWorldRenderer iWorldRenderer = (IWorldRenderer) Wrapper.INSTANCE.getWorldRenderer();
        ShaderProgram shader = ShaderHelper.INSTANCE.getOutlineShader();

        if (event instanceof EventRender3D eventRender3D) {
            checkResize();
            shader.setUpdateUniforms(() -> {
                shader.getUniform("Projection").setMatrix(Matrix4x4.copyFromColumnMajor(Matrix4f.projectionMatrix(0.0f, Wrapper.INSTANCE.getMinecraft().getFramebuffer().textureWidth, Wrapper.INSTANCE.getMinecraft().getFramebuffer().textureHeight, 0.0f, 0.1f, 1000.0f)));
                shader.getUniform("Width").setInt(storageESP.lineWidthProperty.value());
                shader.getUniform("Glow").setBoolean(storageESP.glowProperty.value());
                shader.getUniform("GlowIntensity").setFloat(storageESP.glowIntensityProperty.value());
            });
            //render block entities
            first.beginWrite(false);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                if (storageESP.isValid(blockEntity)) {
                    renderTileEntity(blockEntity, eventRender3D.getPoseStack(), storageESP);
                }
            });
            bufferBuilder.clear();
            BufferRenderer.drawWithShader(bufferBuilder.end());

            //render shader effect
            this.first.endWrite();
            this.first.beginRead();
            float f = this.second.textureWidth;
            float g = this.second.textureHeight;
            RenderSystem.viewport(0, 0, (int)f, (int)g);
            shader.bind();
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
        }
        if (event instanceof EventWorldRender eventWorldRender) {
            if (eventWorldRender.getMode() == EventWorldRender.Mode.PRE) {
                if (Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher() == null || Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera == null)
                    return;
                //setup the entityOutlinesFramebuffer in WorldRenderer because it gets auto applied there
                Framebuffer originalEntityOutlinesFramebuffer = iWorldRenderer.getEntityOutlinesFramebuffer();
                iWorldRenderer.setEntityOutlinesFramebuffer(first);
                //begin write on first fbo then render entities
                first.beginWrite(false);
                first.clear(MinecraftClient.IS_SYSTEM_MAC);
                checkResize();
                RenderSystem.teardownOverlayColor();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                OutlineVertexConsumerProvider outlineVertexConsumerProvider = Wrapper.INSTANCE.getMinecraft().getBufferBuilders().getOutlineVertexConsumers();
                for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                    if (storageESP.isValid(entity)) {
                        int i = storageESP.getColor(entity);
                        int j = 255;
                        int k = i >> 16 & 0xFF;
                        int l = i >> 8 & 0xFF;
                        int m = i & 0xFF;
                        outlineVertexConsumerProvider.setColor(k, l, m, j);
                        Render3DHelper.INSTANCE.renderEntity(eventWorldRender.getPoseStack(), outlineVertexConsumerProvider, entity, eventWorldRender.getPartialTicks(), Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera);
                    }
                }
                outlineVertexConsumerProvider.draw();
                //revert entityOutlinesFramebuffer in WorldRenderer
                iWorldRenderer.setEntityOutlinesFramebuffer(originalEntityOutlinesFramebuffer);
                Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
            }
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

    @Override
    public void enable() {
        super.enable();
    }

    private void renderTileEntity(BlockEntity blockEntity, MatrixStack matrixStack, StorageESP esp) {
        BlockState blockState = blockEntity.getCachedState();

        blockState.getOutlineShape(Wrapper.INSTANCE.getWorld(), blockEntity.getPos()).getBoundingBoxes().forEach(bb -> {
            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            Color color1 = ColorHelper.INSTANCE.getColor(esp.getColor(blockEntity));
            Box box = bb.offset(Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ()));
            float minX = (float)box.minX;
            float minY = (float)box.minY;
            float minZ = (float)box.minZ;
            float maxX = (float)box.maxX;
            float maxY = (float)box.maxY;
            float maxZ = (float)box.maxZ;
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
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
        });
    }
}
