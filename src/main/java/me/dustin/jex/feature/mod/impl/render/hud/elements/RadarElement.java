package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.feature.mod.impl.world.Radar;
import me.dustin.jex.feature.mod.impl.world.Waypoints;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.awt.*;

public class RadarElement extends HudElement{
    public RadarElement(float x, float y, float minWidth, float minHeight) {
        super("Radar", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen))
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xff606060, 0x50000000, 1);
        float midPos = this.getWidth() / 2.0f - 1;
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + midPos, this.getY() + 1, this.getX() + midPos + 1, this.getY() + this.getHeight() - 1, 0xff606060);
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 1, this.getY() + midPos, this.getX() + this.getWidth() - 1, this.getY() + midPos + 1, 0xff606060);

        if (Wrapper.INSTANCE.getWorld() != null)
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (!Radar.INSTANCE.isValid(entity))
                    continue;
                float xPos = (float) (entity.getX() - Wrapper.INSTANCE.getLocalPlayer().getX()) + midPos + this.getX();
                float yPos = (float) (entity.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ()) + midPos + this.getY();
                if (xPos < this.getX() + this.getWidth() - 2 && yPos < this.getY() + this.getHeight() - 2 && yPos > this.getY() + 2 && xPos > this.getX() + 2) {
                    Render2DHelper.INSTANCE.fill(matrixStack, xPos, yPos, xPos + 1, yPos + 1, ESP.INSTANCE.getColor(entity));
                }
            }
        if (Radar.INSTANCE.waypoints) {
            matrixStack.push();
            float scale = 0.75f;
            matrixStack.scale(scale, scale, 1);
            Waypoints.waypoints.forEach(waypoint -> {
                float xPos = (float) (waypoint.getX() - Wrapper.INSTANCE.getLocalPlayer().getX()) + midPos + this.getX();
                float yPos = (float) (waypoint.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ()) + midPos + this.getY();
                String letter = waypoint.getName().substring(0, 1);
                if (xPos < this.getX() + this.getWidth() - 2 && yPos < this.getY() + this.getHeight() - 2 && yPos > this.getY() + 2 && xPos > this.getX() + 2) {
                    FontHelper.INSTANCE.drawCenteredString(matrixStack, letter, xPos / scale, yPos / scale, waypoint.getColor());
                }
            });
            matrixStack.pop();
        }
        try {
            matrixStack.push();
            matrixStack.translate(this.getX() + midPos + 0.5, this.getY() + midPos + 0.5, 0);
            Render2DHelper.INSTANCE.fill(matrixStack, -0.5f, -0.5f, 0.5f, 0.5f, ColorHelper.INSTANCE.getClientColor());
            matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), PlayerHelper.INSTANCE.getYaw() + 180, true));
            drawPointer(matrixStack);
            matrixStack.pop();
        }catch (Exception e) { matrixStack.pop(); }
        FontHelper.INSTANCE.drawCenteredString(matrixStack, "N", this.getX() + midPos + 1, this.getY() + 2, -1);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, "S", this.getX() + midPos + 1, this.getY() + this.getHeight() - 11, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "W", this.getX() + 3, this.getY() + (this.getWidth() / 2) - 5, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "E", this.getX() + this.getWidth() - 1 - FontHelper.INSTANCE.getStringWidth("E"), this.getY() + (this.getWidth() / 2) - 5, -1);
    }

    @Override
    public boolean isVisible() {
        return Radar.INSTANCE.getState();
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
}
