package me.dustin.jex.feature.mod.impl.render.esp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2DNoScale;
import me.dustin.jex.event.render.EventWorldRender;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.load.impl.IWorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;

public class ShaderESP extends FeatureExtension {

    public ShaderESP() {
        super(ESP.Mode.SHADER, ESP.class);
    }

    private final Framebuffer first = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);
    private final Framebuffer second = new SimpleFramebuffer(Wrapper.INSTANCE.getWindow().getFramebufferWidth(), Wrapper.INSTANCE.getWindow().getFramebufferHeight(), false, false);
    private int lastWidth, lastHeight;

    @Override
    public void pass(Event event) {
        if (event instanceof EventWorldRender eventWorldRender) {
            if (eventWorldRender.getMode() == EventWorldRender.Mode.PRE) {
                IWorldRenderer iWorldRenderer = (IWorldRenderer) Wrapper.INSTANCE.getWorldRenderer();
                ShaderProgram shader = ShaderHelper.INSTANCE.getOutlineShader();
                if (Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher() == null || Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera == null)
                    return;
                //setup the entityOutlinesFramebuffer in WorldRenderer because it gets auto applied there
                Framebuffer originalEntityOutlinesFramebuffer = iWorldRenderer.getEntityOutlinesFramebuffer();
                iWorldRenderer.setEntityOutlinesFramebuffer(first);
                //begin write on first fbo then render entities
                first.beginWrite(false);
                first.clear(false);
                RenderSystem.depthFunc(519);
                checkResize();
                RenderSystem.teardownOverlayColor();
                RenderSystem.setShaderColor(1, 1, 1, 1);

                OutlineVertexConsumerProvider outlineVertexConsumerProvider = Wrapper.INSTANCE.getMinecraft().getBufferBuilders().getOutlineVertexConsumers();
                for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                    if (ESP.INSTANCE.isValid(entity)) {
                        int i = ESP.INSTANCE.getColor(entity);
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
                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
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
        } else if (event instanceof EventRender2DNoScale) {
            //render frame buffer
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
