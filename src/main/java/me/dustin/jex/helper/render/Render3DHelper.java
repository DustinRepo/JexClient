package me.dustin.jex.helper.render;

import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static org.lwjgl.opengl.GL11.*;

public enum Render3DHelper {
    INSTANCE;

    public Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(x, y, z);
    }

    public Vec3d getRenderPosition(double x, double y, double z, double partial) {
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
        glRotated(-MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
        glRotated(-MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
    }

    public void applyCameraRots() {
        Camera camera = Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera;
        glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1.0D, 0.0D, 0.0D);
        glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0D), 0.0D, 1.0D, 0.0D);
    }

    public void drawBox(Box bb, int color) {
        glPushMatrix();
        glEnable(GL_LINE_SMOOTH);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Render2DHelper.INSTANCE.glColor(color & 0x70ffffff);

        drawFilledBox(bb);

        Render2DHelper.INSTANCE.glColor(color);
        glLineWidth(1);
        drawOutlineBox(bb);

        glColor4f(1, 1, 1, 1);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();

    }

    public void drawEntityBox(Entity entity, float partialTicks, int color) {
        Vec3d renderPos = getEntityRenderPosition(entity, partialTicks);
        drawEntityBox(entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public void drawEntityBox(Entity entity, double x, double y, double z, int color) {
        glPushMatrix();
        glTranslated(x, y, z);
        glRotatef(-entity.yaw, 0, 1, 0);
        glTranslated(-x, -y, -z);


        glEnable(GL_LINE_SMOOTH);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Render2DHelper.INSTANCE.glColor(color & 0x70ffffff);

        Box bb = new Box(x - entity.getWidth() + 0.25, y, z - entity.getWidth() + 0.25, x + entity.getWidth() - 0.25, y + entity.getHeight() + 0.1, z + entity.getWidth() - 0.25);
        if (entity instanceof ItemEntity)
            bb = new Box(x - 0.15, y + 0.1f, z - 0.15, x + 0.15, y + 0.5, z + 0.15);


        drawFilledBox(bb);

        Render2DHelper.INSTANCE.glColor(color);
        glLineWidth(1);
        drawOutlineBox(bb);

        glTranslated(x, y, z);
        glRotatef(entity.yaw, 0, 1, 0);
        glTranslated(-x, -y, -z);


        glColor4f(1, 1, 1, 1);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_LINE_SMOOTH);
        glPopMatrix();
    }

    public double interpolate(final double now, final double then, final double percent) {
        return (then + (now - then) * percent);
    }

    public void drawFilledBox(Box bb) {
        glBegin(GL_QUADS);
        {
            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.minY, bb.maxZ);

            glVertex3d(bb.minX, bb.maxY, bb.minZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);

            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);
        }
        glEnd();
    }

    public void drawOutlineBox(Box bb) {
        glBegin(GL_LINES);
        {
            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.minY, bb.maxZ);

            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.minY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.minY, bb.minZ);

            glVertex3d(bb.minX, bb.minY, bb.minZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);

            glVertex3d(bb.maxX, bb.minY, bb.maxZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.minY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.minZ);

            glVertex3d(bb.maxX, bb.maxY, bb.minZ);
            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

            glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.maxZ);

            glVertex3d(bb.minX, bb.maxY, bb.maxZ);
            glVertex3d(bb.minX, bb.maxY, bb.minZ);
        }
        glEnd();
    }
}
