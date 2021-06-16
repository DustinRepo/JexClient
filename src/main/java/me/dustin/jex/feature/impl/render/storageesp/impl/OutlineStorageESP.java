package me.dustin.jex.feature.impl.render.storageesp.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender2DNoScale;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.FeatureExtension;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.render.storageesp.StorageESP;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;

public class OutlineStorageESP extends FeatureExtension {
    private StorageESP storageESP;
    public OutlineStorageESP() {
        super("Shader", StorageESP.class);
    }

    @Override
    public void pass(Event event) {
        if (storageESP == null) {
            storageESP = (StorageESP) Feature.get(StorageESP.class);
        }
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D)event;
            if (ShaderHelper.canDrawFBO()) {
                RenderSystem.depthFunc(519);
                ShaderHelper.storageFBO.clear(MinecraftClient.IS_SYSTEM_MAC);
                ShaderHelper.storageFBO.beginWrite(false);
                RenderSystem.teardownOverlayColor();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
                WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                    if (storageESP.isValid(blockEntity)) {
                        renderTileEntity(blockEntity, eventRender3D, storageESP);
                    }
                });
                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);

                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.enableTexture();
                RenderSystem.resetTextureMatrix();
                RenderSystem.depthMask(false);
                ShaderHelper.storageShader.render(Wrapper.INSTANCE.getMinecraft().getTickDelta());
                RenderSystem.enableTexture();
                RenderSystem.depthMask(true);
               Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
            }
        } else if (event instanceof EventRender2DNoScale) {
            if (ShaderHelper.canDrawFBO()) {
                ShaderHelper.drawStorageFBO();
                Wrapper.INSTANCE.getMinecraft().getFramebuffer().beginWrite(true);
            }
        }
    }

    @Override
    public void enable() {
        try {
            if (Wrapper.INSTANCE.getMinecraft().worldRenderer != null)
                ShaderHelper.load();
        } catch (Exception e) {
            System.out.println("Loading");
        }
        super.enable();
    }

    private void renderTileEntity(BlockEntity blockEntity, EventRender3D eventRender3D, StorageESP esp) {
        BlockState blockState = blockEntity.getCachedState();

        blockState.getOutlineShape(Wrapper.INSTANCE.getWorld(), blockEntity.getPos()).getBoundingBoxes().forEach(bb -> {
            Matrix4f matrix4f = eventRender3D.getMatrixStack().peek().getModel();
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

            //Render3DHelper.INSTANCE.drawFilledBox(eventRender3D.getMatrixStack(), bb.offset(Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ())), esp.getColor(blockEntity));
        });
    }

}
