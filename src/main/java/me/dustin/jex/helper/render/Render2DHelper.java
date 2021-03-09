package me.dustin.jex.helper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.FloatBuffer;

public enum Render2DHelper {
    INSTANCE;
    protected Identifier cog = new Identifier("jex", "gui/click/cog.png");

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public double getScaleFactor() {
        return Wrapper.INSTANCE.getWindow().getScaleFactor();
    }

    public int getScaledWidth() {
        return Wrapper.INSTANCE.getWindow().getScaledWidth();
    }

    public int getScaledHeight() {
        return Wrapper.INSTANCE.getWindow().getScaledHeight();
    }

    public void drawTexture(MatrixStack matrices, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    private void drawTexture(MatrixStack matrices, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    private void drawTexture(MatrixStack matrices, float x0, float y0, float x1, float y1, int z, float regionWidth, float regionHeight, float u, float v, int textureWidth, int textureHeight) {
        drawTexturedQuad(matrices.peek().getModel(), x0, y0, x1, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    public void drawTexturedQuad(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
        bufferBuilder.end();
        RenderSystem.enableAlphaTest();
        BufferRenderer.draw(bufferBuilder);
    }

    public void fill(MatrixStack matrixStack, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = matrixStack.peek().getModel();
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

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void drawRect(float x, float y, float x2, float y2, int color) {
        float j;
        if (x < x2) {
            j = x;
            x = x2;
            x2 = j;
        }

        if (y < y2) {
            j = y;
            y = y2;
            y2 = j;
        }

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        glColor(color);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public Color hexToColor(String value) {
        String digits;
        if (value.startsWith("#")) {
            digits = value.substring(1, Math.min(value.length(), 7));
        } else {
            digits = value;
        }
        String hstr = "0x" + digits;
        Color c;
        try {
            c = Color.decode(hstr);
        } catch (NumberFormatException nfe) {
            c = null;
        }
        return c;
    }

    public void drawFace(MatrixStack matrixStack, float x, float y, int renderScale, Identifier id) {
        Wrapper.INSTANCE.getMinecraft().getTextureManager().bindTexture(id);
        drawTexture(matrixStack, x, y, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
        drawTexture(matrixStack, x, y, 8 * renderScale, 8 * renderScale, 40 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
    }

    public void fillAndBorder(MatrixStack matrixStack, float left, float top, float right, float bottom, int bcolor, int icolor, float f) {
        fill(matrixStack, left + f, top + f, right - f, bottom - f, icolor);
        fill(matrixStack, left, top, left + f, bottom, bcolor);
        fill(matrixStack, left + f, top, right, top + f, bcolor);
        fill(matrixStack, left + f, bottom - f, right, bottom, bcolor);
        fill(matrixStack, right - f, top + f, right, bottom - f, bcolor);
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

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);

        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glPopMatrix();
    }

    public void drawFullCircle(int cx, int cy, double r, int c) {
        r *= 2.0D;
        cx *= 2;
        cy *= 2;
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        enableGL2D();
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(6);
        for (int i = 0; i <= 360; i++) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            GL11.glVertex2d(cx + x, cy + y);
        }
        GL11.glEnd();
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        disableGL2D();
    }

    public void drawArc(float cx, float cy, double r, int c, int startpoint, double arc, int linewidth) {
        r *= 2.0D;
        cx *= 2;
        cy *= 2;
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        enableGL2D();
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glLineWidth(linewidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = (int) startpoint; i <= arc; i += 1) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            GL11.glVertex2d(cx + x, cy + y);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        disableGL2D();
    }

    public void drawHLine(MatrixStack matrixStack, float par1, float par2, float par3, int par4) {
        if (par2 < par1) {
            float var5 = par1;
            par1 = par2;
            par2 = var5;
        }

        fill(matrixStack, par1, par3, par2 + 1, par3 + 1, par4);
    }

    public void drawVLine(MatrixStack matrixStack, float par1, float par2, float par3, int par4) {
        if (par3 < par2) {
            float var5 = par2;
            par2 = par3;
            par3 = var5;
        }

        fill(matrixStack, par1, par2 + 1, par1 + 1, par3, par4);
    }

    public void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
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
        Vec2f vec2f = new Vec2f(MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY());
        float distance = ClientMathHelper.INSTANCE.getDistance2D(vec2f, new Vec2f(centerX, centerY));
        return distance <= radius;
    }

    public boolean isOnScreen(Vec3d pos) {
        if (pos.getZ() > -1 && pos.getZ() < 1) {
                return true;
        }
        return false;
    }

    public void drawCheck(float x, float y, int color) {
        GL11.glPushMatrix();
        GL11.glScaled(1.1, 1.1, 1.1);
        x /= 1.1;
        y /= 1.1;
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        glColor(color);
        GL11.glLineWidth(2);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x + 1, y + 1);
        GL11.glVertex2d(x + 3, y + 4);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x + 3, y + 4);
        GL11.glVertex2d(x + 6, y - 3);
        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public void drawItem(ItemStack stack, float xPosition, float yPosition) {
        String amountText = stack.getCount() != 1 ? stack.getCount() + "" : "";
        IItemRenderer iItemRenderer = (IItemRenderer) Wrapper.INSTANCE.getMinecraft().getItemRenderer();
        iItemRenderer.renderItemIntoGUI(stack, xPosition, yPosition);
        renderGuiItemOverlay(Wrapper.INSTANCE.getMinecraft().textRenderer, stack, xPosition - 0.5f, yPosition + 1, amountText);
    }

    public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, float x, float y, @Nullable String countLabel) {
        if (!stack.isEmpty()) {
            MatrixStack matrixStack = new MatrixStack();
            if (stack.getCount() != 1 || countLabel != null) {
                String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
                matrixStack.translate(0.0D, 0.0D, (double) (Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset + 200.0F));
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                renderer.draw(string, (float) (x + 19 - 2 - renderer.getWidth(string)), (float) (y + 6 + 3), 16777215, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
                immediate.draw();
            }

            if (stack.isDamaged()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuffer();
                float f = (float) stack.getDamage();
                float g = (float) stack.getMaxDamage();
                float h = Math.max(0.0F, (g - f) / g);
                int i = Math.round(13.0F - f * 13.0F / g);
                int j = MathHelper.hsvToRgb(h / 3.0F, 1.0F, 1.0F);
                this.renderGuiQuad(bufferBuilder, x + 2, y + 13, 13, 2, 0, 0, 0, 255);
                this.renderGuiQuad(bufferBuilder, x + 2, y + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
            float k = clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(), MinecraftClient.getInstance().getTickDelta());
            if (k > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator2 = Tessellator.getInstance();
                BufferBuilder bufferBuilder2 = tessellator2.getBuffer();
                this.renderGuiQuad(bufferBuilder2, x, y + MathHelper.floor(16.0F * (1.0F - k)), 16, MathHelper.ceil(16.0F * k), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }

    private void renderGuiQuad(BufferBuilder buffer, float x, float y, float width, float height, int red, int green, int blue, int alpha) {
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        buffer.vertex((double) (x + 0), (double) (y + 0), 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex((double) (x + 0), (double) (y + height), 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex((double) (x + width), (double) (y + height), 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex((double) (x + width), (double) (y + 0), 0.0D).color(red, green, blue, alpha).next();
        Tessellator.getInstance().draw();
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

    public Vec3d to2D(Vec3d worldPos) {
        Vec3d bound = Render3DHelper.INSTANCE.getRenderPosition(worldPos);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    private Vec3d to2D(double x, double y, double z) {
        int displayHeight = Wrapper.INSTANCE.getWindow().getHeight();
        Vector3f screenCoords = new Vector3f();
        int[] viewport = new int[4];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer modelView = stack.mallocFloat(16);
            FloatBuffer projection = stack.mallocFloat(16);
            GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView);
            GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projection);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            new org.joml.Matrix4f(projection).mul(new org.joml.Matrix4f(modelView)).project((float) x, (float) y, (float) z, viewport, screenCoords);
        }
        return new Vec3d(screenCoords.x / Render2DHelper.INSTANCE.getScaleFactor(), (displayHeight - screenCoords.y) / Render2DHelper.INSTANCE.getScaleFactor(), screenCoords.z);
    }

    public Vec3d getHeadPos(Entity entity, float partialTicks) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y + entity.getHeight() + 0.2, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getFootPos(Entity entity, float partialTicks) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getPos(Entity entity, float yOffset, float partialTicks) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y + yOffset, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public void drawArrow(MatrixStack matrixStack, float x, float y, boolean open, int color) {
        glColor(color);
        Wrapper.INSTANCE.getMinecraft().getTextureManager().bindTexture(cog);
        DrawableHelper.drawTexture(matrixStack, (int) x - 5, (int) y - 5, 0, 0, 10, 10, 10, 10);
        GL11.glColor4f(1, 1, 1, 1);
        /*GL11.glPushMatrix();
        GL11.glScaled(1.3, 1.3, 1.3);
        if (open) {
            y -= 1.5f;
            x += 2;
        }
        x /= 1.3;
        y /= 1.3;
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        glColor(color);
        GL11.glLineWidth(2);
        if (open) {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x + 4, y + 3);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(x + 4, y + 3);
            GL11.glVertex2d(x, y + 6);
            GL11.glEnd();
        } else {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(x, y);
            GL11.glVertex2d(x + 3, y + 4);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(x + 3, y + 4);
            GL11.glVertex2d(x + 6, y);
            GL11.glEnd();
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();*/
    }
}
