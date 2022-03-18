package me.dustin.jex.gui.click.dropdown.theme.flare;

import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Consumer;

public class FlareDropdownToggleButton extends DropdownButton {
    private boolean toggled;
    public FlareDropdownToggleButton(DropdownWindow window, String name, float x, float y, float width, float height, Consumer<Void> consumer) {
        super(window, name, x, y, width, height, consumer);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getName(), getX() + 2, getY()  + (getHeight() / 2 - 4), -1);
        int colors[] = isToggled() ? new int[]{0xff007a21, 0xff004600} : new int[]{0xff990014, 0xff550000};
        Render2DHelper.INSTANCE.gradientFill(matrixStack, getX() + getWidth() - 8 - 10, getY() + 2, getX() + getWidth() - 4, getY() + getHeight() - 2, colors[0], colors[1]);
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX() + getWidth() - 8 - 10 - 1, getY() + 2 - 1, getX() + getWidth() - 4 + 1, getY() + getHeight() - 2 + 1, 0xff999999, 0x00ffffff, 1);
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}
