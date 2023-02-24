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
import me.dustin.jex.helper.render.shader.post.impl.PostProcessOutline;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IWorldRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class OutlineStorageESP extends FeatureExtension {
    private StorageESP storageESP;
    public OutlineStorageESP() {
        super(StorageESP.Mode.SHADER, StorageESP.class);
    }

    private final PostProcessOutline postProcessOutline = new PostProcessOutline();

    @Override
    public void pass(Event event) {
        if (storageESP == null)
            storageESP = Feature.get(StorageESP.class);
        IWorldRenderer iWorldRenderer = (IWorldRenderer) Wrapper.INSTANCE.getWorldRenderer();
        ShaderProgram shader = postProcessOutline.getShader();

        if (event instanceof EventWorldRender eventWorldRender) {
            if (eventWorldRender.getMode() == EventWorldRender.Mode.PRE) {
                if (Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher() == null || Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera == null)
                    return;
                shader.setUpdateUniforms(() -> {
                    shader.getUniform("Width").setInt(storageESP.lineWidthProperty.value());
                    shader.getUniform("Glow").setBoolean(storageESP.glowProperty.value());
                    shader.getUniform("GlowIntensity").setFloat(storageESP.glowIntensityProperty.value());
                });
                //setup the entityOutlinesFramebuffer in WorldRenderer because it gets auto applied there
                Framebuffer originalEntityOutlinesFramebuffer = iWorldRenderer.getEntityOutlinesFramebuffer();
                iWorldRenderer.setEntityOutlinesFramebuffer(postProcessOutline.getFirst());

                //begin write on first fbo
                postProcessOutline.getFirst().beginWrite(false);
                postProcessOutline.getFirst().clear(MinecraftClient.IS_SYSTEM_MAC);
                RenderSystem.teardownOverlayColor();
                RenderSystem.setShaderColor(1, 1, 1, 1);

                //render block entities and entities
                OutlineVertexConsumerProvider outlineVertexConsumerProvider = Wrapper.INSTANCE.getMinecraft().getBufferBuilders().getOutlineVertexConsumers();
                WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                    if (storageESP.isValid(blockEntity)) {
                        int i = storageESP.getColor(blockEntity);
                        int j = 255;
                        int k = i >> 16 & 0xFF;
                        int l = i >> 8 & 0xFF;
                        int m = i & 0xFF;
                        outlineVertexConsumerProvider.setColor(k, l, m, j);
                        Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getPos());
                        eventWorldRender.getPoseStack().push();
                        eventWorldRender.getPoseStack().translate(vec3d.x, vec3d.y, vec3d.z);
                        BlockEntityRenderer<BlockEntity> blockEntityBlockEntityRenderer = Wrapper.INSTANCE.getMinecraft().getBlockEntityRenderDispatcher().get(blockEntity);
                        if (blockEntityBlockEntityRenderer != null)
                            blockEntityBlockEntityRenderer.render(blockEntity, eventWorldRender.getPoseStack(), outlineVertexConsumerProvider, eventWorldRender.getPartialTicks(), 0xF000F0, OverlayTexture.DEFAULT_UV);
                        eventWorldRender.getPoseStack().pop();
                    }
                });
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

                postProcessOutline.render();
            }
        } else if (event instanceof EventRender2DNoScale) {
            int width = Wrapper.INSTANCE.getWindow().getFramebufferWidth();
            int height = Wrapper.INSTANCE.getWindow().getFramebufferHeight();
            RenderSystem.enableBlend();
            postProcessOutline.getSecond().draw(width, height, false);
            Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
        }
    }
}
