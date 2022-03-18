package me.dustin.jex.gui.click.dropdown.impl.option;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.option.types.ColorOption;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.MouseHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class ColorOptionButton extends DropdownOptionButton {
    protected final ColorOption colorOption;
    protected final Identifier colorSlider = new Identifier("jex", "gui/click/colorslider.png");
    private boolean isSliding;
    public ColorOptionButton(DropdownWindow window, ColorOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
        this.colorOption = option;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        super.render(matrixStack);
    }

    protected void drawGradientRect(MatrixStack matrixStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float g = (float) (startColor >> 16 & 255) / 255.0F;
        float h = (float) (startColor >> 8 & 255) / 255.0F;
        float i = (float) (startColor & 255) / 255.0F;
        float j = (float) (endColor >> 24 & 255) / 255.0F;
        float k = (float) (endColor >> 16 & 255) / 255.0F;
        float l = (float) (endColor >> 8 & 255) / 255.0F;
        float m = (float) (endColor & 255) / 255.0F;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, (float) right, (float) top, (float) 0).color(g, h, i, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) top, (float) 0).color(1, 1, 1, f).next();
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, (float) 0).color(0, 0, 0, j).next();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, (float) 0).color(k, l, m, j).next();

        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        super.click(double_1, double_2, int_1);
        if (isHovered() && int_1 == 0) {
            isSliding = true;
        }
    }

    @Override
    public void tick() {
        if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
            isSliding = false;
            if (JexClient.INSTANCE.isAutoSaveEnabled())
                ConfigManager.INSTANCE.get(FeatureFile.class).write();
        }
        if (isSliding)
            handleSliders(colorOption);
        super.tick();
    }

    protected void handleSliders(ColorOption colorOption) {
    }

    public boolean isSliding() {
        return isSliding;
    }

    public void setSliding(boolean sliding) {
        isSliding = sliding;
    }
}
