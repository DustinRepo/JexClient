package me.dustin.jex.helper.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.opengl.GL11;

import java.awt.*;


public enum Render3DHelper {
    INSTANCE;

    public Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(x, y, z);
    }

    public Vec3d getRenderPosition(double x, double y, double z) {
        double minX = x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public Vec3d getRenderPosition(Vec3d vec3d) {
        double minX = vec3d.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = vec3d.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = vec3d.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public Vec3d getRenderPosition(BlockPos blockPos) {
        double minX = blockPos.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double minY = blockPos.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double minZ = blockPos.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public void fixCameraRots() {
        Camera camera = Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera;
        GL11.glRotated(-MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
        GL11.glRotated(-MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
    }

    public void applyCameraRots() {
        Camera camera = Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera;
        GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
        GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
    }

    public void setup3DRender(boolean disableDepth) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        if (disableDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();
    }

    public void end3DRender() {
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public void drawSphere(MatrixStack matrixStack, float radius, int gradation, int color, boolean testDepth, Vec3d pos) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        final float PI = 3.141592f;
        float x, y, z, alpha, beta;
        setup3DRender(!testDepth);
        for (alpha = 0.0f; alpha < Math.PI; alpha += PI / gradation) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(1, VertexFormats.POSITION_COLOR);
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += PI / gradation) {
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha)));
                Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                x = (float) (pos.getX() +  (radius * Math.cos(beta) * Math.sin(alpha + PI / gradation)));
                y = (float) (pos.getY() +  (radius * Math.sin(beta) * Math.sin(alpha + PI / gradation)));
                z = (float) (pos.getZ() +  (radius * Math.cos(alpha + PI / gradation)));
                renderPos = Render3DHelper.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
            }
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
        }
        end3DRender();
    }

    public void drawBox(MatrixStack matrixStack, Box bb, int color) {
        setup3DRender(true);
        drawFilledBox(matrixStack, bb, color & 0x70ffffff);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);
        end3DRender();
    }

    public void drawBoxOutline(MatrixStack matrixStack, Box bb, int color) {
        setup3DRender(true);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);
        end3DRender();
    }

    public void drawBoxInside(MatrixStack matrixStack, Box bb, int color) {
        setup3DRender(true);
        drawFilledBox(matrixStack, bb, color & 0x70ffffff);
        end3DRender();
    }

    public void drawEntityBox(MatrixStack matrixStack, Entity entity, float partialTicks, int color) {
        Vec3d renderPos = getEntityRenderPosition(entity, partialTicks);
        drawEntityBox(matrixStack, entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public void drawEntityBox(MatrixStack matrixStack, Entity entity, double x, double y, double z, int color) {
        setup3DRender(true);

        Box bb = new Box(x - entity.getWidth() + 0.25, y, z - entity.getWidth() + 0.25, x + entity.getWidth() - 0.25, y + entity.getHeight() + 0.1, z + entity.getWidth() - 0.25);
        if (entity instanceof ItemEntity)
            bb = new Box(x - 0.15, y + 0.1f, z - 0.15, x + 0.15, y + 0.5, z + 0.15);

        drawFilledBox(matrixStack, bb, color & 0x60ffffff);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixStack, bb, color);

        end3DRender();
    }

    public double interpolate(final double now, final double then, final double percent) {
        return (then + (now - then) * percent);
    }

    public void drawFilledBox(MatrixStack matrixStack, Box bb, int color) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7/*QUADS*/, VertexFormats.POSITION_COLOR);
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
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    public void drawOutlineBox(MatrixStack matrixStack, Box bb, int color) {
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        Matrix4f matrix4f = matrixStack.peek().getModel();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(1/*LINES*/, VertexFormats.POSITION_COLOR);

        VoxelShape shape = VoxelShapes.cuboid(bb);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
            bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        });

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }
}
