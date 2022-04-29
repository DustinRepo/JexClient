package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.feature.mod.impl.world.Radar;
import me.dustin.jex.feature.mod.impl.world.Waypoints;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap;

import java.awt.*;

public class RadarElement extends HudElement{
    public RadarElement(float x, float y, float minWidth, float minHeight) {
        super("Radar", x, y, minWidth, minHeight);
    }

    NativeImage radarImage = null;

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
                if (waypoint.getDimension().equalsIgnoreCase(WorldHelper.INSTANCE.getDimensionID().toString())) {
                    float xPos = (float) (waypoint.getX() - Wrapper.INSTANCE.getLocalPlayer().getX()) + midPos + this.getX();
                    float yPos = (float) (waypoint.getZ() - Wrapper.INSTANCE.getLocalPlayer().getZ()) + midPos + this.getY();
                    String letter = waypoint.getName().substring(0, 1);
                    if (xPos < this.getX() + this.getWidth() - 2 && yPos < this.getY() + this.getHeight() - 2 && yPos > this.getY() + 2 && xPos > this.getX() + 2) {
                        FontHelper.INSTANCE.drawCenteredString(matrixStack, letter, xPos / scale, yPos / scale, waypoint.getColor());
                    }
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

            /*Identifier id = new Identifier("jex", "radar/map.png");
            if (radarImage == null) {
                if (Wrapper.INSTANCE.getWorld() != null) {
                    radarImage = createNativeImage();
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(radarImage));
                }
            } else {
                Render2DHelper.INSTANCE.bindTexture(id);
                DrawableHelper.drawTexture(matrixStack, (int) getX(), (int) getY(), 0, 0, (int)getWidth(), (int)getHeight(), (int)getWidth(), (int)getHeight());

            }*/
        }catch (Exception e) { matrixStack.pop(); }
        FontHelper.INSTANCE.drawCenteredString(matrixStack, "N", this.getX() + midPos + 1, this.getY() + 2, -1);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, "S", this.getX() + midPos + 1, this.getY() + this.getHeight() - 11, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "W", this.getX() + 3, this.getY() + (this.getWidth() / 2) - 5, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "E", this.getX() + this.getWidth() - 1 - FontHelper.INSTANCE.getStringWidth("E"), this.getY() + (this.getWidth() / 2) - 5, -1);
    }

    public NativeImage createNativeImage() {
        NativeImage nativeImage = new NativeImage((int)getWidth(), (int)getHeight(), true);
        for (int x = -nativeImage.getWidth() / 2; x < nativeImage.getWidth() / 2; x++)
            for (int z = -nativeImage.getHeight() / 2; z < nativeImage.getHeight() / 2; z++) {
                BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, 0, z);
                int y = Wrapper.INSTANCE.getWorld().getChunk(blockPos.getX() / 16, blockPos.getZ() / 16).sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
                Block block = WorldHelper.INSTANCE.getBlock(new BlockPos(blockPos.getX(), y, blockPos.getZ()));
                nativeImage.setColor(x, z, block.getDefaultMapColor().color);
            }
        nativeImage.close();
        return nativeImage;
    }

    @Override
    public boolean isVisible() {
        return Radar.INSTANCE.getState();
    }

    private void drawPointer(MatrixStack matrixStack) {
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        Color color1 = ColorHelper.INSTANCE.getColor(ColorHelper.INSTANCE.getClientColor());

        Render2DHelper.INSTANCE.setup2DRender(false);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f,0, -4, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f,-1, 0, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f,1, 0, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f,0, -4, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.clear();
        BufferRenderer.drawWithShader(bufferBuilder.end());

        Render2DHelper.INSTANCE.end2DRender();
    }
}
