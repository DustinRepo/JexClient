package me.dustin.jex.helper.render;

import java.awt.Color;
import java.util.ArrayList;

import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum Render3DHelper {
    INSTANCE;

    public Vec3 getEntityRenderPosition(Entity entity, double partial, PoseStack poseStack) {
        Matrix4f matrix = poseStack.last().pose();
        double x = entity.xOld + ((entity.getX() - entity.xOld) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double y = entity.yOld + ((entity.getY() - entity.yOld) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double z = entity.zOld + ((entity.getZ() - entity.zOld) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        Vector4f vector4f = new Vector4f((float)x, (float)y, (float)z, 1.f);
        vector4f.transform(matrix);
        return new Vec3(vector4f.x(), vector4f.y(), vector4f.z());
    }

    public Vec3 getRenderPosition(double x, double y, double z, PoseStack poseStack) {
        Matrix4f matrix = poseStack.last().pose();
        double minX = x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double minY = y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double minZ = z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        Vector4f vector4f = new Vector4f((float)minX, (float)minY, (float)minZ, 1.f);
        vector4f.transform(matrix);
        return new Vec3(vector4f.x(), vector4f.y(), vector4f.z());
    }

    public Vec3 getRenderPosition(Vec3 vec3d, PoseStack poseStack) {
        Matrix4f matrix = poseStack.last().pose();
        double minX = vec3d.x() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double minY = vec3d.y() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double minZ = vec3d.z() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        Vector4f vector4f = new Vector4f((float)minX, (float)minY, (float)minZ, 1.f);
        vector4f.transform(matrix);
        return new Vec3(vector4f.x(), vector4f.y(), vector4f.z());
    }

    public Vec3 getRenderPosition(BlockPos blockPos, PoseStack poseStack) {
        Matrix4f matrix = poseStack.last().pose();
        double minX = blockPos.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double minY = blockPos.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double minZ = blockPos.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        Vector4f vector4f = new Vector4f((float)minX, (float)minY, (float)minZ, 1.f);
        vector4f.transform(matrix);
        return new Vec3(vector4f.x(), vector4f.y(), vector4f.z());
    }

    public Vec3 getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.xOld + ((entity.getX() - entity.xOld) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double y = entity.yOld + ((entity.getY() - entity.yOld) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double z = entity.zOld + ((entity.getZ() - entity.zOld) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        return new Vec3(x, y, z);
    }

    public Vec3 getRenderPosition(double x, double y, double z) {
        double minX = x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double minY = y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double minZ = z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        return new Vec3(minX, minY, minZ);
    }

    public Vec3 getRenderPosition(Vec3 vec3d) {
        double minX = vec3d.x() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double minY = vec3d.y() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double minZ = vec3d.z() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        return new Vec3(minX, minY, minZ);
    }

    public Vec3 getRenderPosition(BlockPos blockPos) {
        double minX = blockPos.getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
        double minY = blockPos.getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
        double minZ = blockPos.getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;
        return new Vec3(minX, minY, minZ);
    }

    public void fixCameraRots(PoseStack poseStack) {
        Camera camera = Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera;
        poseStack.mulPose(Vector3f.YN.rotationDegrees(camera.getYRot() + 180.0F));
        poseStack.mulPose(Vector3f.XN.rotationDegrees(camera.getXRot()));
    }

    public void applyCameraRots(PoseStack poseStack) {
        Camera camera = Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera;
        poseStack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot() + 180.0F));
    }

    public void setup3DRender(boolean disableDepth) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
        else
            RenderSystem.enableDepthTest();
        RenderSystem.depthMask(Minecraft.useFancyGraphics());
        RenderSystem.enableCull();
    }

    public void end3DRender() {
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    public void drawSphere(PoseStack poseStack, float radius, int gradation, int color, boolean testDepth, Vec3 pos) {
        Matrix4f matrix4f = poseStack.last().pose();
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        final float PI = 3.141592f;
        float x, y, z, alpha, beta;
        if (!testDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        for (alpha = 0.0f; alpha < Math.PI; alpha += PI / gradation) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += PI / gradation) {
                x = (float) (pos.x() +  (radius * Math.cos(beta) * Math.sin(alpha)));
                y = (float) (pos.y() +  (radius * Math.sin(beta) * Math.sin(alpha)));
                z = (float) (pos.z() +  (radius * Math.cos(alpha)));
                Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
                x = (float) (pos.x() +  (radius * Math.cos(beta) * Math.sin(alpha + PI / gradation)));
                y = (float) (pos.y() +  (radius * Math.sin(beta) * Math.sin(alpha + PI / gradation)));
                z = (float) (pos.z() +  (radius * Math.cos(alpha + PI / gradation)));
                renderPos = Render3DHelper.INSTANCE.getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float)renderPos.x, (float)renderPos.y, (float)renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
            }
            bufferBuilder.clear();
            BufferUploader.drawWithShader(bufferBuilder.end());
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
    }

    public void drawBoxWithDepthTest(PoseStack matrixstack, AABB bb, int color) {
        setup3DRender(false);
        drawFilledBox(matrixstack, bb, color & 0x50ffffff);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixstack, bb, color);
        end3DRender();
    }

    public void drawBox(PoseStack matrixstack, AABB bb, int color) {
        setup3DRender(true);
        drawFilledBox(matrixstack, bb, color & 0x50ffffff);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixstack, bb, color);
        end3DRender();
    }

    public void drawBoxOutline(PoseStack matrixstack, AABB bb, int color) {
        setup3DRender(true);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixstack, bb, color);
        end3DRender();
    }

    public void drawBoxInside(PoseStack matrixstack, AABB bb, int color) {
        setup3DRender(true);
        drawFilledBox(matrixstack, bb, color & 0x50ffffff);
        end3DRender();
    }

    public void drawEntityBox(PoseStack matrixstack, Entity entity, float partialTicks, int color) {
        Vec3 renderPos = getEntityRenderPosition(entity, partialTicks);
        drawEntityBox(matrixstack, entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public void drawEntityBox(PoseStack matrixstack, Entity entity, double x, double y, double z, int color) {
        float yaw = EntityHelper.INSTANCE.getYaw(entity);
        setup3DRender(true);
        matrixstack.translate(x, y, z);
        matrixstack.mulPose(new Quaternion(new Vector3f(0, -1, 0), yaw, true));
        matrixstack.translate(-x, -y, -z);

        AABB bb = new AABB(x - entity.getBbWidth() + 0.25, y, z - entity.getBbWidth() + 0.25, x + entity.getBbWidth() - 0.25, y + entity.getBbWidth() + 0.1, z + entity.getBbWidth() - 0.25);
        if (entity instanceof ItemEntity)
            bb = new AABB(x - 0.15, y + 0.1f, z - 0.15, x + 0.15, y + 0.5, z + 0.15);


        drawFilledBox(matrixstack, bb, color & 0x25ffffff);
        RenderSystem.lineWidth(1.5f);
        drawOutlineBox(matrixstack, bb, color);

        end3DRender();
        matrixstack.translate(x, y, z);
        matrixstack.mulPose(new Quaternion(new Vector3f(0, 1, 0), yaw, true));
        matrixstack.translate(-x, -y, -z);
    }

    public double interpolate(final double now, final double then, final double percent) {
        return (then + (now - then) * percent);
    }

    public void drawList(PoseStack poseStack, ArrayList<BoxStorage> list, boolean disableDepth) {
		setup3DRender(disableDepth);
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    	list.forEach(blockStorage -> {
            AABB box = blockStorage.box();
            int color = blockStorage.color();
            drawOutlineBox(poseStack, box, color, false);
    	});
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        list.forEach(blockStorage -> {
            AABB box = blockStorage.box();
            int color = blockStorage.color();
            drawFilledBox(poseStack, box, color & 0x70ffffff, false);
    	});
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        end3DRender();
    }
    
    public void drawFilledBox(PoseStack poseStack, AABB bb, int color) {
    	drawFilledBox(poseStack, bb, color, true);
    }

    public void directionTranslate(PoseStack poseStack, Direction direction) {
        switch (direction) {
            case UP -> poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), 180, true));
            case NORTH -> {
                poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), 90, true));
            }
            case SOUTH -> {
                poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), -90, true));
            }
            case WEST -> {
                poseStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), 90, true));
                poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), 90, true));
            }
            case EAST -> {
                poseStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), 90, true));
                poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), -90, true));
            }
        }
    }

    public void drawFilledBox(PoseStack poseStack, AABB bb, int color, boolean draw) {
        Matrix4f matrix4f = poseStack.last().pose();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        if (draw)
        	bufferBuilder.begin(VertexFormat.Mode.QUADS/*QUADS*/, DefaultVertexFormat.POSITION_COLOR);
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

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
        if (draw) {
	        BufferUploader.drawWithShader(bufferBuilder.end());
            bufferBuilder.clear();
        }
    }

    public void drawFadeBox(PoseStack poseStack, AABB bb, int color) {
        Matrix4f matrix4f = poseStack.last().pose();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS/*QUADS*/, DefaultVertexFormat.POSITION_COLOR);
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        bufferBuilder.clear();
    }

    public void doFadeBoxNoDraw(PoseStack poseStack, AABB bb, int color) {
        Matrix4f matrix4f = poseStack.last().pose();
        Color color1 = ColorHelper.INSTANCE.getColor(color);

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        float minX = (float)bb.minX;
        float minY = (float)bb.minY;
        float minZ = (float)bb.minZ;
        float maxX = (float)bb.maxX;
        float maxY = (float)bb.maxY;
        float maxZ = (float)bb.maxZ;

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();

        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), 0).endVertex();
    }

    public void drawOutlineBox(PoseStack poseStack, AABB bb, int color) {
    	drawOutlineBox(poseStack, bb, color, true);
    }

    public void drawOutlineBox(PoseStack poseStack, AABB bb, int color, boolean draw) {
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        Matrix4f matrix4f = poseStack.last().pose();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        if (draw)
        	bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES/*LINES*/, DefaultVertexFormat.POSITION_COLOR);

        VoxelShape shape = Shapes.create(bb);
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
            bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        });
        if (draw) {
	        BufferUploader.drawWithShader(bufferBuilder.end());
            bufferBuilder.clear();
        }
    }

    public record BoxStorage (AABB box, int color) {}
}
