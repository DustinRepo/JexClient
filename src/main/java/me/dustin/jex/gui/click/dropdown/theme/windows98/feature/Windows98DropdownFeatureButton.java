package me.dustin.jex.gui.click.dropdown.theme.windows98.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownFeatureButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;

public class Windows98DropdownFeatureButton extends DropdownFeatureButton {
    public Windows98DropdownFeatureButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        drawW98Button(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight());
        FontHelper.INSTANCE.draw(matrixStack, getFeature().getName(), getX() + 2, getY() + getHeight() / 2.f - 4, !getFeature().getState() ? 0xff83888c : isHovered() ? -1 : 0xff000000);
        super.render(matrixStack);
    }

    private void drawW98Button(MatrixStack matrixStack, float x, float y, float endX, float endY) {
        Render2DHelper.INSTANCE.setup2DRender(false);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, y, endX, endY, !isHovered() ? 0xffbfbfbf : getWindow().getColor());

        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, y, endX - 2, y + 1, isHovered() ? 0xff000000 : -1);
        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, y, x + 1, endY - 1, isHovered() ? 0xff000000 : -1);

        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, endY - 1, endX - 1, endY, 0xff000000);
        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, endX - 1, y, endX, endY, 0xff000000);
        bufferBuilder.clear();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        Render2DHelper.INSTANCE.end2DRender();
    }
}
