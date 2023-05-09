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
import me.dustin.jex.helper.render.shader.post.impl.PostProcessOutline;
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

    private final PostProcessOutline postProcessOutline = new PostProcessOutline();

    @Override
    public void pass(Event event) {
        if (event instanceof EventWorldRender eventWorldRender) {
            if (eventWorldRender.getMode() == EventWorldRender.Mode.PRE) {
                IWorldRenderer iWorldRenderer = (IWorldRenderer) Wrapper.INSTANCE.getWorldRenderer();
                ShaderProgram shader = postProcessOutline.getShader();
                shader.setUpdateUniforms(() -> {
                    shader.getUniform("Width").setInt(ESP.INSTANCE.lineWidthProperty.value());
                    shader.getUniform("Glow").setBoolean(ESP.INSTANCE.glowProperty.value());
                    shader.getUniform("GlowIntensity").setFloat(ESP.INSTANCE.glowIntensityProperty.value());
                });

                if (Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher() == null || Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera == null)
                    return;
                //setup the entityOutlinesFramebuffer in WorldRenderer because it gets auto applied there
                Framebuffer originalEntityOutlinesFramebuffer = iWorldRenderer.getEntityOutlinesFramebuffer();
                iWorldRenderer.setEntityOutlinesFramebuffer(postProcessOutline.getFirst());
                //begin write on first fbo then render entities
                postProcessOutline.getFirst().beginWrite(false);
                postProcessOutline.getFirst().clear(false);
                RenderSystem.depthFunc(519);
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
                this.postProcessOutline.render();
            }
        } else if (event instanceof EventRender2DNoScale) {
            //render frame buffer
            int width = Wrapper.INSTANCE.getWindow().getFramebufferWidth();
            int height = Wrapper.INSTANCE.getWindow().getFramebufferHeight();
            RenderSystem.enableBlend();
            this.postProcessOutline.getSecond().draw(width, height, false);
            Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
        }
    }
}
