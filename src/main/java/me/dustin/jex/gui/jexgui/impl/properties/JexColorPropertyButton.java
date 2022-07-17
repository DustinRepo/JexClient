package me.dustin.jex.gui.jexgui.impl.properties;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.impl.JexPropertyButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.BufferHelper;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;
import java.util.ArrayList;

public class JexColorPropertyButton extends JexPropertyButton {
    private final Identifier colorSlider = new Identifier("jex", "gui/click/colorslider.png");
    private final Property<Color> colorProperty;
    private boolean isSliding;
    public JexColorPropertyButton(Property<Color> colorProperty, float x, float y, float width, float height, ArrayList<Button> buttonList, int color) {
        super(colorProperty, x, y, width, height, buttonList, color);
        this.colorProperty = colorProperty;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getBackgroundColor());
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);float[] hsb = toHSB();
        float huepos = hsb[0] * 80;

        float satpos = hsb[1] * 80;
        float brightpos = ((1 - hsb[2])) * 79;


        Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, -1, 0xff000000);
        drawGradientRect(matrixStack, this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, ColorHelper.INSTANCE.getColorViaHue(hsb[0] * 270).getRGB());
        Render2DHelper.INSTANCE.drawGradientRect(this.getX() + 5, this.getY() + 15, this.getX() + 85, this.getY() + 95, 0x20000000, 0xff000000);
        //color cursor
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 5 + satpos - 1, this.getY() + 15 + brightpos - 1, this.getX() + 5 + satpos + 1, this.getY() + 15 + brightpos + 1, -1);

        //hue slider
        Render2DHelper.INSTANCE.bindTexture(colorSlider);
        DrawableHelper.drawTexture(matrixStack, (int) this.getX() + (int) this.getWidth() - 10, (int) this.getY() + 15, 0, 0, 5, 80, 10, 80);
        //hue cursor
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + this.getWidth() - 10, this.getY() + 15 + huepos - 1, (this.getX() + this.getWidth() - 10) + 5, this.getY() + 15 + huepos + 1, 0xff000000);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, getColorProperty().getName(), this.getX() + 3, this.getY() + 3, getColorProperty().value().getRGB());
        super.render(matrixStack);
    }

    public void handleSlider() {
        float[] hsb = toHSB();
        if (MouseHelper.INSTANCE.getMouseX() > this.getX() + (this.getWidth() / 2.f)) {
            float position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
            float percent = MathHelper.clamp(position / 79, 0, 1);
            float value = percent * 270;
            hsb[0] = value;
        } else {
            hsb[0] *= 270;
            float position = MouseHelper.INSTANCE.getMouseX() - (this.getX() + 5);
            float percent = MathHelper.clamp(position / 80, 0, 1);
            hsb[1] = percent;

            position = MouseHelper.INSTANCE.getMouseY() - (this.getY() + 15);
            percent = MathHelper.clamp(position / 79, 0, 1);
            hsb[2] = 1 - percent;
        }
        getColorProperty().setValue(ColorHelper.INSTANCE.getColorViaHue(hsb[0], hsb[1], hsb[2]));
    }

    @Override
    public void tick() {
        if (isSliding) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
                return;
            }
            handleSlider();
        }
        super.tick();
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0) {
            isSliding = true;
        }
        super.click(double_1, double_2, int_1);
    }

    private float[] toHSB() {
        return Color.RGBtoHSB(getColorProperty().value().getRed(), getColorProperty().value().getGreen(), getColorProperty().value().getBlue(), null);
    }

    protected void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float g = (float) (startColor >> 16 & 255) / 255.0F;
        float h = (float) (startColor >> 8 & 255) / 255.0F;
        float i = (float) (startColor & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, (float) right, (float) top, (float) 0).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) top, (float) 0).color(1, 1, 1, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, (float) 0).color(0, 0, 0, 255).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, (float) 0).color(0, 0, 0, 255).next();

        BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public Property<Color> getColorProperty() {
        return this.colorProperty;
    }
}
