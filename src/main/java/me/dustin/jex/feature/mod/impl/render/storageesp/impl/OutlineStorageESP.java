package me.dustin.jex.feature.mod.impl.render.storageesp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventHasOutline;
import me.dustin.jex.event.render.EventTeamColor;
import me.dustin.jex.event.render.EventRender2DNoScale;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.storageesp.StorageESP;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.awt.*;

public class OutlineStorageESP extends FeatureExtension {
    private StorageESP storageESP;
    public OutlineStorageESP() {
        super("Shader", StorageESP.class);
    }

    @Override
    public void pass(Event event) {
        if (storageESP == null) {
            storageESP = Feature.get(StorageESP.class);
        }
        if (event instanceof EventRender3D eventRender3D) {
            if (ShaderHelper.INSTANCE.canDrawFBO()) {
                RenderSystem.depthFunc(519);
                ShaderHelper.INSTANCE.storageFBO.clear(Minecraft.ON_OSX);
                ShaderHelper.INSTANCE.storageFBO.bindWrite(false);
                RenderSystem.teardownOverlayColor();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.QUADS/*QUADS*/, DefaultVertexFormat.POSITION_COLOR);
                WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                    if (storageESP.isValid(blockEntity)) {
                        renderTileEntity(blockEntity, eventRender3D, storageESP);
                    }
                });
                bufferBuilder.clear();
                BufferUploader.drawWithShader(bufferBuilder.end());
                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.enableTexture();
                RenderSystem.resetTextureMatrix();
                RenderSystem.depthMask(false);
                ShaderHelper.INSTANCE.storageShader.process(Wrapper.INSTANCE.getMinecraft().getFrameTime());
                RenderSystem.enableTexture();
                RenderSystem.depthMask(true);
                Wrapper.INSTANCE.getMinecraft().getMainRenderTarget().bindWrite(true);
            }
        } else if (event instanceof EventRender2DNoScale) {
            if (ShaderHelper.INSTANCE.canDrawFBO()) {
                ShaderHelper.INSTANCE.drawStorageFBO();
                Wrapper.INSTANCE.getMinecraft().getMainRenderTarget().bindWrite(true);
            }
        } else if (event instanceof EventHasOutline eventHasOutline) {
            if (storageESP.isValid(eventHasOutline.getEntity())) {
                eventHasOutline.setOutline(true);
                event.cancel();
            }
        } else if (event instanceof EventTeamColor eventTeamColor) {
            if (storageESP.isValid(eventTeamColor.getEntity())) {
                eventTeamColor.setColor(storageESP.getColor(eventTeamColor.getEntity()));
                event.cancel();
            }
        }
    }

    @Override
    public void enable() {
        if (Wrapper.INSTANCE.getMinecraft().levelRenderer != null)
            ShaderHelper.INSTANCE.load();
        super.enable();
    }

    private void renderTileEntity(BlockEntity blockEntity, EventRender3D eventRender3D, StorageESP esp) {
        BlockState blockState = blockEntity.getBlockState();

        blockState.getShape(Wrapper.INSTANCE.getWorld(), blockEntity.getBlockPos()).toAabbs().forEach(bb -> {
            Matrix4f matrix4f = eventRender3D.getPoseStack().last().pose();
            Color color1 = ColorHelper.INSTANCE.getColor(esp.getColor(blockEntity));
            AABB box = bb.move(Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ()));
            float minX = (float)box.minX;
            float minY = (float)box.minY;
            float minZ = (float)box.minZ;
            float maxX = (float)box.maxX;
            float maxY = (float)box.maxY;
            float maxZ = (float)box.maxZ;
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
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
        });
    }
}
