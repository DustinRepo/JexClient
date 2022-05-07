package me.dustin.jex.gui.click.dropdown.theme.windows98;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.renderer.GameRenderer;
import java.util.function.Consumer;

public class Windows98DropdownToggleButton extends DropdownButton {
    private boolean toggled;
    public Windows98DropdownToggleButton(DropdownWindow window, String name, float x, float y, float width, float height, Consumer<Void> consumer) {
        super(window, name, x, y, width, height, consumer);
    }

    @Override
    public void render(PoseStack matrixStack) {
        drawW98Button(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight());
        FontHelper.INSTANCE.draw(matrixStack, getName(), getX() + 2, getY() + getHeight() / 2.f - 4, !isToggled() ? 0xff83888c : isHovered() ? -1 : 0xff000000);
    }

    private void drawW98Button(PoseStack matrixStack, float x, float y, float endX, float endY) {
        Render2DHelper.INSTANCE.setup2DRender(false);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, y, endX, endY, !isHovered() ? 0xffbfbfbf : getWindow().getColor());

        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, y, endX - 2, y + 1, isHovered() ? 0xff000000 : -1);
        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, y, x + 1, endY - 1, isHovered() ? 0xff000000 : -1);

        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x, endY - 1, endX - 1, endY, 0xff000000);
        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, endX - 1, y, endX, endY, 0xff000000);
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        Render2DHelper.INSTANCE.end2DRender();
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}
