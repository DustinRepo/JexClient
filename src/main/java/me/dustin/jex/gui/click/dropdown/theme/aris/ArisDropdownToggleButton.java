package me.dustin.jex.gui.click.dropdown.theme.aris;

import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;

public class ArisDropdownToggleButton extends DropdownButton {
    private boolean toggled;
    public ArisDropdownToggleButton(DropdownWindow window, String name, float x, float y, float width, float height, Consumer<Void> consumer) {
        super(window, name, x, y, width, height, consumer);
    }

    @Override
    public void render(PoseStack matrixStack) {
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xff000000, isToggled() ? 0xff333333 : 0xff202020);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x45000000);
        if (isToggled())
            Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX(), getY() + 0.5f, getX() + getWidth() - 0.5f, 0xff4a4a4a);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, getName(), getX() + getWidth() / 2.f, getY()  + (getHeight() / 2 - 4), -1);
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
}
