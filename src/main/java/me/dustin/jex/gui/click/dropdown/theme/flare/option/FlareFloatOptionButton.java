package me.dustin.jex.gui.click.dropdown.theme.flare.option;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.option.types.FloatOption;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.option.FloatOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class FlareFloatOptionButton extends FloatOptionButton {
    public FlareFloatOptionButton(DropdownWindow window, FloatOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FloatOption v = (FloatOption) getOption();

        float startV = v.getValue() - v.getMin();

        float pos = (startV / (v.getMax() - v.getMin())) * (this.getWidth() - 8);

        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX() + 3, this.getY() + this.getHeight() - 5, this.getX() + 4 + (getWidth() - 7), this.getY() + this.getHeight() - 1,  0x99aaaaaa, 0xff000000, 1);

        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 4, this.getY() + this.getHeight() - 4, this.getX() + 4 + pos, this.getY() + this.getHeight() - 2, 0xff00ffff);
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 4 + pos - 1, this.getY() + this.getHeight() - 6, this.getX() + 4 + pos + 1, this.getY() + this.getHeight() - 1, getWindow().getColor());
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + 2, getY() + (getHeight() / 2 - 4), isOpen() ? 0xff00ffff : -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, v.getName() + ": " + v.getValue(), this.getX() + 10, this.getY() + 2, -1);
        if (isOpen()) {
            DropdownButton bottomOption = getVeryBottomOption();
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + 2, getY() + getHeight() + getWindow().getTheme().getOptionOffset() - 2, getX() + getWidth() - 2, bottomOption.getY() + bottomOption.getHeight() + 2, 0xaa999999, 0x50000000, 1);
            this.getChildren().forEach(dropdownButton -> dropdownButton.render(matrixStack));
        }
    }

    @Override
    protected void handleSliders(FloatOption v) {
        float position = MouseHelper.INSTANCE.getMouseX() - (this.getX() + 4);
        float percent = position / (this.getWidth() - 8) * 100;
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
