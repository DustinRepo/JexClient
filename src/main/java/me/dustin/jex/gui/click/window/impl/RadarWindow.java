package me.dustin.jex.gui.click.window.impl;

import me.dustin.jex.feature.mod.impl.world.Waypoints;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.feature.mod.impl.world.Radar;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

public class RadarWindow extends Window{
    public RadarWindow(String name, float x, float y, float width, float height) {
        super(name, x, y, width, height);
        this.setPinned(true);
        this.setOpen(true);
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        if (!Radar.INSTANCE.getState())
            return;
        super.draw(matrixStack);
        if (!isOpen() || Wrapper.INSTANCE.getLocalPlayer() == null)
            return;
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), this.getY() + this.getHeight() + this.getWidth(), 0xff606060, 0x50000000, 1);
        float midPos = this.getWidth() / 2.0f - 1;
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + midPos, this.getY() + this.getHeight() + 1, this.getX() + midPos + 1, this.getY() + this.getHeight() + this.getWidth() - 1, 0xff606060);
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 1, this.getY() + this.getHeight() + midPos, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() + midPos + 1, 0xff606060);

        if (Wrapper.INSTANCE.getWorld() != null)
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (!Radar.INSTANCE.isValid(entity))
                    continue;
                float xPos = (float) (entity.getX() - Wrapper.INSTANCE.getLocalPlayer().getX()) + midPos + this.getX();
                float yPos = (float) (entity.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ()) + midPos + this.getY() + this.getHeight();
                if (xPos < this.getX() + this.getWidth() - 2 && yPos < this.getY() + this.getHeight() + this.getWidth() - 2 && yPos > this.getY() + this.getHeight() + 2 && xPos > this.getX() + 2) {
                    Render2DHelper.INSTANCE.fill(matrixStack, xPos, yPos, xPos + 1, yPos + 1, ESP.INSTANCE.getColor(entity));
                }
            }
        if (Radar.INSTANCE.waypoints) {
            matrixStack.push();
            float scale = 0.75f;
            matrixStack.scale(scale, scale, 1);
            Waypoints.waypoints.forEach(waypoint -> {
                float xPos = (float) (waypoint.getX() - Wrapper.INSTANCE.getLocalPlayer().getX()) + midPos + this.getX();
                float yPos = (float) (waypoint.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ()) + midPos + this.getY() + this.getHeight();
                String letter = waypoint.getName().substring(0, 1);
                    if (xPos < this.getX() + this.getWidth() - 2 && yPos < this.getY() + this.getHeight() + this.getWidth() - 2 && yPos > this.getY() + this.getHeight() + 2 && xPos > this.getX() + 2) {
                        FontHelper.INSTANCE.drawCenteredString(matrixStack, letter, xPos / scale, yPos / scale, waypoint.getColor());
                    }
            });
            matrixStack.pop();
        }
        matrixStack.push();
        matrixStack.translate(this.getX() + midPos + 0.5, this.getY() + this.getHeight() + midPos + 0.5, 0);
        Render2DHelper.INSTANCE.fill(matrixStack, -0.5f, -0.5f, 0.5f, 0.5f, ColorHelper.INSTANCE.getClientColor());
        matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), PlayerHelper.INSTANCE.getYaw() + 180, true));
        drawPointer(matrixStack);
        matrixStack.pop();
        FontHelper.INSTANCE.drawCenteredString(matrixStack, "N", this.getX() + midPos + 1, this.getY() + this.getHeight() + 2, -1);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, "S", this.getX() + midPos + 1, this.getY() + this.getHeight() + this.getWidth() - 11, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "W", this.getX() + 2, this.getY() + this.getHeight() + (this.getWidth() / 2) - 5, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "E", this.getX() + this.getWidth() - 2 - FontHelper.INSTANCE.getStringWidth("E"), this.getY() + this.getHeight() + (this.getWidth() / 2) - 5, -1);
    }

    private void drawPointer(MatrixStack matrixStack) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        Color color1 = ColorHelper.INSTANCE.getColor(ColorHelper.INSTANCE.getClientColor());

        Render2DHelper.INSTANCE.setup2DRender(false);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f,0, -4, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f,-1, 0, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f,1, 0, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f,0, -4, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        Render2DHelper.INSTANCE.end2DRender();
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (!Radar.INSTANCE.getState())
            return;
        super.click(double_1, double_2, int_1);
    }
}
