package me.dustin.jex.helper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.Matrix4x4;
import me.dustin.jex.helper.math.vector.Vector3D;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public enum Render2DHelper {
    INSTANCE;
    private final ResourceLocation cog = new ResourceLocation("jex", "gui/click/cog.png");
    private final static ResourceLocation MAP_BACKGROUND = new ResourceLocation("textures/map/map_background_checkerboard.png");

    public void setup2DRender(boolean disableDepth) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
    }

    public void end2DRender() {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    public double getScaleFactor() {
        return Wrapper.INSTANCE.getWindow().getGuiScale();
    }

    public int getScaledWidth() {
        return Wrapper.INSTANCE.getWindow().getGuiScaledWidth();
    }

    public int getScaledHeight() {
        return Wrapper.INSTANCE.getWindow().getGuiScaledHeight();
    }

    public void drawTexture(PoseStack matrices, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    private void drawTexture(PoseStack matrices, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    private void drawTexture(PoseStack matrices, float x0, float y0, float x1, float y1, int z, float regionWidth, float regionHeight, float u, float v, int textureWidth, int textureHeight) {
        drawTexturedQuad(matrices.last().pose(), x0, y0, x1, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    public void drawTexturedQuad(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrices, (float)x0, (float)y1, (float)z).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrices, (float)x1, (float)y1, (float)z).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrices, (float)x1, (float)y0, (float)z).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrices, (float)x0, (float)y0, (float)z).uv(u0, v0).endVertex();
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public void drawTexturedQuadNoDraw(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.vertex(matrices, (float)x0, (float)y1, (float)z).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrices, (float)x1, (float)y1, (float)z).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrices, (float)x1, (float)y0, (float)z).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrices, (float)x0, (float)y0, (float)z).uv(u0, v0).endVertex();
    }

    public void fill(PoseStack poseStack, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = poseStack.last().pose();
        float j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void fillNoDraw(PoseStack poseStack, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = poseStack.last().pose();
        float j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(g, h, k, f).endVertex();
    }

    public void drawFace(PoseStack poseStack, float x, float y, int renderScale, ResourceLocation id) {
        try {
            bindTexture(id);
            drawTexture(poseStack, x, y, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
            drawTexture(poseStack, x, y, 8 * renderScale, 8 * renderScale, 40 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
        }catch (Exception e){}
    }

    public void fillAndBorder(PoseStack poseStack, float left, float top, float right, float bottom, int bcolor, int icolor, float f) {
        fill(poseStack, left + f, top + f, right - f, bottom - f, icolor);
        fill(poseStack, left, top, left + f, bottom, bcolor);
        fill(poseStack, left + f, top, right, top + f, bcolor);
        fill(poseStack, left + f, bottom - f, right, bottom, bcolor);
        fill(poseStack, right - f, top + f, right, bottom - f, bcolor);
    }

    public void drawGradientRect(double x, double y, double x2, double y2, int col1, int col2) {
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(x2, y, 0).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(x, y, 0).color(f1, f2, f3, f).endVertex();

        bufferBuilder.vertex(x, y2, 0).color(f5, f6, f7, f4).endVertex();
        bufferBuilder.vertex(x2, y2, 0).color(f5, f6, f7, f4).endVertex();

        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void gradientFill(PoseStack poseStack, float x, float y, float x2, float y2, int col1, int col2) {
        Matrix4f matrix4f = poseStack.last().pose();
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x2, y, 0).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(f1, f2, f3, f).endVertex();

        bufferBuilder.vertex(matrix4f, x, y2, 0).color(f5, f6, f7, f4).endVertex();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(f5, f6, f7, f4).endVertex();

        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void gradientSidewaysFill(PoseStack poseStack, float x, float y, float x2, float y2, int col1, int col2) {
        Matrix4f matrix4f = poseStack.last().pose();
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x2, y, 0).color(f5, f6, f7, f4).endVertex();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(f1, f2, f3, f).endVertex();

        bufferBuilder.vertex(matrix4f, x, y2, 0).color(f1, f2, f3, f).endVertex();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(f5, f6, f7, f4).endVertex();

        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void outlineAndFill(PoseStack poseStack, float x, float y, float x2, float y2, int bcolor, int icolor) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float f = (float)(icolor >> 24 & 255) / 255.0F;
        float g = (float)(icolor >> 16 & 255) / 255.0F;
        float h = (float)(icolor >> 8 & 255) / 255.0F;
        float k = (float)(icolor & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.vertex(matrix, x, y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x2, y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        f = (float)(bcolor >> 24 & 255) / 255.0F;
        g = (float)(bcolor >> 16 & 255) / 255.0F;
        h = (float)(bcolor >> 8 & 255) / 255.0F;
        k = (float)(bcolor & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x, y2, 0.0F).color(g, h, k, f).endVertex();

        bufferBuilder.vertex(matrix, x, y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).endVertex();

        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x2, y, 0.0F).color(g, h, k, f).endVertex();

        bufferBuilder.vertex(matrix, x2, y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void drawMap(PoseStack poseStack, int x, int y, ItemStack stack) {
        MapItemSavedData mapState = MapItem.getSavedData(stack, Wrapper.INSTANCE.getWorld());
        if (mapState != null) {
            Render2DHelper.INSTANCE.bindTexture(MAP_BACKGROUND);
            GuiComponent.blit(poseStack, x, y, 0, 0, 150, 150, 150, 150);

            poseStack.pushPose();
            poseStack.translate(x + 11, y + 11, 1000);
            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            Wrapper.INSTANCE.getMinecraft().gameRenderer.getMapRenderer().render(poseStack, immediate, MapItem.getMapId(stack), mapState, false, 15728880);
            immediate.endBatch();
            poseStack.popPose();
        }
    }

    public void drawCheckmark(PoseStack poseStack, float x, float y, int color) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.vertex(matrix, x, y + 5, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x + 3, y + 8, 0.0F).color(g, h, k, f).endVertex();

        bufferBuilder.vertex(matrix, x + 3, y + 8, 0.0F).color(g, h, k, f).endVertex();
        bufferBuilder.vertex(matrix, x + 9, y - 1, 0.0F).color(g, h, k, f).endVertex();

        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void drawFullCircle(int cx, int cy, double r, int c, PoseStack poseStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, -64).color(f1, f2, f3, f).endVertex();
        }
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
    }

    public void drawArc(float cx, float cy, double r, int c, int startpoint, double arc, int linewidth, PoseStack poseStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(linewidth);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);//TRIANGLE_STRIP is fucked too I guess

        for (int i = (int) startpoint; i <= arc; i += 1) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, 0).color(f1, f2, f3, f).endVertex();
        }
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
    }

    public void drawHLine(PoseStack poseStack, float par1, float par2, float par3, int par4) {
        if (par2 < par1) {
            float var5 = par1;
            par1 = par2;
            par2 = var5;
        }

        fill(poseStack, par1, par3, par2 + 1, par3 + 1, par4);
    }

    public void drawThinHLine(PoseStack poseStack, float x, float y, float endX, int color) {
        Matrix4f matrix4f = poseStack.last().pose();
        Color color1 = ColorHelper.INSTANCE.getColor(color);
        Render2DHelper.INSTANCE.setup2DRender(false);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, x, y, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.vertex(matrix4f, endX, y, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        Render2DHelper.INSTANCE.end2DRender();
    }

    public void drawVLine(PoseStack poseStack, float par1, float par2, float par3, int par4) {
        if (par3 < par2) {
            float var5 = par2;
            par2 = par3;
            par3 = var5;
        }

        fill(poseStack, par1, par2 + 1, par1 + 1, par3, par4);
    }

    public Color hex2Rgb(String colorStr) {
        try {
            return new Color(Integer.valueOf(colorStr.substring(2, 4), 16), Integer.valueOf(colorStr.substring(4, 6), 16), Integer.valueOf(colorStr.substring(6, 8), 16));
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    public boolean isHovered(float x, float y, float width, float height) {
        return x < MouseHelper.INSTANCE.getMouseX() && x + width > MouseHelper.INSTANCE.getMouseX() && y < MouseHelper.INSTANCE.getMouseY() && y + height > MouseHelper.INSTANCE.getMouseY();
    }

    public boolean hoversCircle(float centerX, float centerY, float radius) {
        Vec2 vec2f = new Vec2(MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY());
        float distance = ClientMathHelper.INSTANCE.getDistance2D(vec2f, new Vec2(centerX, centerY));
        return distance <= radius;
    }

    public boolean isOnScreen(Vec3 pos) {
        if (pos.z() > -1 && pos.z() < 1) {
            return true;
        }
        return false;
    }

    public void drawItem(ItemStack stack, float xPosition, float yPosition) {
        drawItem(stack, xPosition, yPosition, 1);
    }
    public void drawItem(ItemStack stack, float xPosition, float yPosition, float scale) {
        String amountText = stack.getCount() != 1 ? stack.getCount() + "" : "";
        IItemRenderer iItemRenderer = (IItemRenderer) Wrapper.INSTANCE.getMinecraft().getItemRenderer();
        iItemRenderer.renderItemIntoGUI(stack, xPosition, yPosition);
        renderGuiItemOverlay(Wrapper.INSTANCE.getMinecraft().font, stack, xPosition - 0.5f, yPosition + 1, scale, amountText);
    }

    public void renderGuiItemOverlay(Font renderer, ItemStack stack, float x, float y, float scale, @Nullable String countLabel) {
        if (!stack.isEmpty()) {
            PoseStack poseStack = new PoseStack();
            if (stack.getCount() != 1 || countLabel != null) {
                String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
                poseStack.translate(0.0D, 0.0D, (double)(Wrapper.INSTANCE.getMinecraft().getItemRenderer().blitOffset + 200.0F));
                MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                renderer.drawInBatch(string, (float)(x + 19 - 2 - renderer.width(string)), (float)(y + 6 + 3), 16777215, true, poseStack.last().pose(), immediate, false, 0, 15728880);
                immediate.endBatch();
            }

            if (stack.isBarVisible()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                int i = stack.getBarWidth();
                int j = stack.getBarColor();
                this.fill(poseStack, x + 2, y + 13, x + 2 + 13, y + 13 + 2, 0xff000000);
                this.fill(poseStack, x + 2, y + 13, x + 2 + i, y + 13 + 1, new Color(j >> 16 & 255, j >> 8 & 255, j & 255, 255).getRGB());
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;
            float f = clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tessellator2 = Tesselator.getInstance();
                BufferBuilder bufferBuilder2 = tessellator2.getBuilder();
                this.renderGuiQuad(bufferBuilder2, x, y + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }

    public void draw3DCape(PoseStack poseStack, float x, float y, ResourceLocation identifier, float yaw, float pitch) {
        poseStack.pushPose();
        poseStack.translate(x + 16, y + 30, 64);
        poseStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), yaw, true));
        poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), pitch, true));
        //
        bindTexture(identifier);
        //front of cape
        GuiComponent.blit(poseStack, -16, -30, 2.5f, 4, 32, 60, 198, 124);
        //back of cape
        poseStack.mulPose(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 180, true));
        GuiComponent.blit(poseStack, -16, -30, 34.5f, 4, 32, 60, 198, 124);
        poseStack.mulPose(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), -180, true));
        //
        poseStack.popPose();
    }

    private void renderGuiQuad(BufferBuilder buffer, float x, float y, float width, float height, int red, int green, int blue, int alpha) {
        buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex((double) (x + 0), (double) (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.vertex((double) (x + 0), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.vertex((double) (x + width), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        buffer.vertex((double) (x + width), (double) (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }

    public int getPercentColor(float percent) {
        if (percent <= 15)
            return new Color(255, 0, 0).getRGB();
        else if (percent <= 25)
            return new Color(255, 75, 92).getRGB();
        else if (percent <= 50)
            return new Color(255, 123, 17).getRGB();
        else if (percent <= 75)
            return new Color(255, 234, 0).getRGB();
        return new Color(0, 255, 0).getRGB();
    }

    public ChatFormatting getPercentFormatting(float percent) {
        if (percent <= 15)
            return ChatFormatting.DARK_RED;
        else if (percent <= 25)
            return ChatFormatting.RED;
        else if (percent <= 50)
            return ChatFormatting.GOLD;
        else if (percent <= 75)
            return ChatFormatting.YELLOW;
        return ChatFormatting.GREEN;
    }

    public Vec3 to2D(Vec3 worldPos, PoseStack poseStack) {
        Vec3 bound = Render3DHelper.INSTANCE.getRenderPosition(worldPos, poseStack);
        Vec3 twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3(twoD.x, twoD.y, twoD.z);
    }

    private Vec3 to2D(double x, double y, double z) {
        int displayHeight = Wrapper.INSTANCE.getWindow().getScreenHeight();
        Vector3D screenCoords = new Vector3D();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Matrix4x4 matrix4x4Proj = Matrix4x4.copyFromColumnMajor(RenderSystem.getProjectionMatrix());//no more joml :)
        Matrix4x4 matrix4x4Model = Matrix4x4.copyFromColumnMajor(RenderSystem.getModelViewMatrix());//but I do the math myself now :( (heck math)
        matrix4x4Proj.mul(matrix4x4Model).project((float) x, (float) y, (float) z, viewport, screenCoords);

        return new Vec3(screenCoords.x / Render2DHelper.INSTANCE.getScaleFactor(), (displayHeight - screenCoords.y) / Render2DHelper.INSTANCE.getScaleFactor(), screenCoords.z);
    }

    public Vec3 getHeadPos(Entity entity, float partialTicks, PoseStack poseStack) {
        Vec3 bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks).add(0, entity.getBbHeight() + 0.2, 0);
        Vector4f vector4f = new Vector4f((float)bound.x, (float)bound.y, (float)bound.z, 1.f);
        vector4f.transform(poseStack.last().pose());
        Vec3 twoD = to2D(vector4f.x(), vector4f.y(), vector4f.z());
        return new Vec3(twoD.x, twoD.y, twoD.z);
    }

    public Vec3 getFootPos(Entity entity, float partialTicks, PoseStack poseStack) {
        Vec3 bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks, poseStack);
        Vec3 twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3(twoD.x, twoD.y, twoD.z);
    }

    public Vec3 getPos(Entity entity, float yOffset, float partialTicks, PoseStack poseStack) {
        Vec3 bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks).add(0, yOffset, 0);
        Vector4f vector4f = new Vector4f((float)bound.x, (float)bound.y, (float)bound.z, 1.f);
        vector4f.transform(poseStack.last().pose());
        Vec3 twoD = to2D(vector4f.x(), vector4f.y(), vector4f.z());
        return new Vec3(twoD.x, twoD.y, twoD.z);
    }

    public void drawArrow(PoseStack poseStack, float x, float y, boolean open, int color) {
        bindTexture(cog);
        shaderColor(color);
        GuiComponent.blit(poseStack, (int) x - 5, (int) y - 5, 0, 0, 10, 10, 10, 10);
        shaderColor(-1);
    }

    public void bindTexture(ResourceLocation identifier) {
        RenderSystem.setShaderTexture(0, identifier);
    }

    public void shaderColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }
}
