package me.dustin.jex.gui.click.dropdown.theme.aris.option;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.option.types.BoolOption;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.option.BooleanOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.option.DropdownOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class ArisBooleanOptionButton extends BooleanOptionButton {
    public ArisBooleanOptionButton(DropdownWindow window, BoolOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        float bottomY = getY() + getHeight();
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            if (bottomOption != null)
                bottomY = bottomOption.getY() + bottomOption.getHeight() + (bottomOption instanceof DropdownOptionButton dropdownOptionButton && dropdownOptionButton.parentButton != null && dropdownOptionButton.parentButton.isOpen() ? getWindow().getTheme().getOptionOffset() : 0) + 2;
        }
        if (bottomY < getWindow().getY() + getWindow().getTheme().getTopBarSize() || getY() > getWindow().getY() + getWindow().getHeight())
            return;
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0xff000000, boolOption.getValue() ? 0xff333333 : 0xff202020);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0x45000000);
        if (boolOption.getValue())
            Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX(), getY() + 0.5f, getX() + getWidth() - 0.5f, 0xff4a4a4a);
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + getWidth() - 10, getY()  + (getHeight() / 2 - 4), -1);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, boolOption.getName(), getX() + getWidth() / 2.f, getY()  + (getHeight() / 2 - 4), -1);
        if (isOpen()) {
            this.getChildren().forEach(dropdownButton -> dropdownButton.render(matrixStack));
        }
    }
}
