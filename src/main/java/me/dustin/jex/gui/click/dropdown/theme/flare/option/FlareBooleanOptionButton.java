package me.dustin.jex.gui.click.dropdown.theme.flare.option;

import me.dustin.jex.feature.option.types.BoolOption;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.option.BooleanOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class FlareBooleanOptionButton extends BooleanOptionButton {
    public FlareBooleanOptionButton(DropdownWindow window, BoolOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + 2, getY()  + (getHeight() / 2 - 4), isOpen() ? 0xff00ffff : -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, boolOption.getName(), getX() + 10, getY()  + (getHeight() / 2 - 4), -1);
        int colors[] = boolOption.getValue() ? new int[]{0xff007a21, 0xff004600} : new int[]{0xff990014, 0xff550000};
        Render2DHelper.INSTANCE.gradientFill(matrixStack, getX() + getWidth() - 8 - 10, getY() + 2, getX() + getWidth() - 4, getY() + getHeight() - 2, colors[0], colors[1]);
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + getWidth() - 8 - 10 - 1, getY() + 2 - 1, getX() + getWidth() - 4 + 1, getY() + getHeight() - 2 + 1, 0xff999999, 0x00ffffff, 1);
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + 2, getY() + getHeight() + getWindow().getTheme().getOptionOffset() - 2, getX() + getWidth() - 2, bottomOption.getY() + bottomOption.getHeight() + 2, 0xaa999999, 0x50000000, 1);
            this.getChildren().forEach(dropdownButton -> dropdownButton.render(matrixStack));
        }
    }
}
