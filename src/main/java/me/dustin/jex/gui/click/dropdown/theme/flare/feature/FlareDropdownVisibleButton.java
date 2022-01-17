package me.dustin.jex.gui.click.dropdown.theme.flare.feature;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownVisibleButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class FlareDropdownVisibleButton extends DropdownVisibleButton {
    public FlareDropdownVisibleButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Visible", getX() + 2, getY()  + (getHeight() / 2 - 4), -1);
        int colors[] = getFeature().isVisible() ? new int[]{0xff007a21, 0xff004600} : new int[]{0xff990014, 0xff550000};
        Render2DHelper.INSTANCE.gradientFill(matrixStack, getX() + getWidth() - 8 - 10, getY() + 2, getX() + getWidth() - 4, getY() + getHeight() - 2, colors[0], colors[1]);
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + getWidth() - 8 - 10 - 1, getY() + 2 - 1, getX() + getWidth() - 4 + 1, getY() + getHeight() - 2 + 1, 0xff999999, 0x00ffffff, 1);
        super.render(matrixStack);
    }
}
