package me.dustin.jex.gui.click.dropdown.theme.aris.option;

import me.dustin.jex.feature.option.types.FloatOption;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.option.DropdownOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.option.FloatOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class ArisFloatOptionButton extends FloatOptionButton {
    public ArisFloatOptionButton(DropdownWindow window, FloatOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        float bottomY = getY() + getHeight();
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            if (bottomOption != null)
                bottomY = bottomOption.getY() + bottomOption.getHeight() + (bottomOption instanceof DropdownOptionButton dropdownOptionButton && dropdownOptionButton.parentButton != null && dropdownOptionButton.parentButton.isOpen() ? getWindow().getTheme().getOptionOffset() : 0) + 2;
        }
        if (bottomY < getWindow().getY() + getWindow().getTheme().getTopBarSize() || getY() > getWindow().getY() + getWindow().getHeight())
            return;
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0xff000000, !isSliding() ? 0xff333333 : 0xff202020);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), bottomY, 0x45000000);
        if (!isSliding())
            Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX(), getY() + 0.5f, getX() + getWidth() - 0.5f, 0xff4a4a4a);
        FloatOption v = (FloatOption) getOption();

        float startV = v.getValue() - v.getMin();

        float pos = (startV / (v.getMax() - v.getMin())) * this.getWidth();

        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY() + this.getHeight() - 5, this.getX() + getWidth(), this.getY() + this.getHeight() - 1, 0xff000000);

        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY() + this.getHeight() - 4, this.getX() + pos, this.getY() + this.getHeight() - 2, 0xff999999);
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + getWidth() - 10, getY() + (getHeight() / 2 - 4), -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, v.getName(), this.getX() + 2, this.getY() + 3, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, String.valueOf(v.getValue()), this.getX() + getWidth() - FontHelper.INSTANCE.getStringWidth(String.valueOf(v.getValue())) - (hasChild() ? 12 : 2), this.getY() + 3, -1);
        if (isOpen()) {
            this.getChildren().forEach(dropdownButton -> dropdownButton.render(matrixStack));
        }
    }

    @Override
    protected void handleSliders(FloatOption v) {
        float position = MouseHelper.INSTANCE.getMouseX() - this.getX();
        float percent = position / this.getWidth() * 100;
        float increment = v.getInc();
        if (percent > 100) {
            percent = 100;
        }
        if (percent < 0) {
            percent = 0;
        }
        float value = (percent / 100) * ((v.getMax() - v.getMin()) + increment);
        value += v.getMin();
        if (value > v.getMax()) {
            value = v.getMax();
        }
        if (value < v.getMin()) {
            value = v.getMin();
        }
        v.setValue((float) ((float) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
        v.setValue((float) ClientMathHelper.INSTANCE.round(v.getValue(), 2));
        super.handleSliders(v);
    }
}
