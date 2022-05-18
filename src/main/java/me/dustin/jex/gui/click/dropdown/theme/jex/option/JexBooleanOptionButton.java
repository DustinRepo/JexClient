package me.dustin.jex.gui.click.dropdown.theme.jex.option;

import me.dustin.jex.feature.option.types.BoolOption;
import me.dustin.jex.gui.click.dropdown.impl.option.BooleanOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class JexBooleanOptionButton extends BooleanOptionButton {
    public JexBooleanOptionButton(DropdownWindow window, BoolOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        if (boolOption.getValue())
            Render2DHelper.INSTANCE.gradientFill(matrixStack, getX() + 2, getY() + 2, getX() + getHeight() - 2, getY() + getHeight() - 2, getWindow().getColor(), ColorHelper.INSTANCE.getColor(getWindow().getColor()).darker().getRGB());
        else
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + 2, getY() + 2, getX() + getHeight() - 2, getY() + getHeight() - 2, getWindow().getColor(), 0x00ffffff, 1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getOption().getName(), getX() + getHeight() - 4 + 4, getY() + (getHeight() / 2 - 4), -1);
        super.render(matrixStack);
    }
}
