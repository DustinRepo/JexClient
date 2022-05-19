package me.dustin.jex.gui.click.dropdown.theme.aris.window;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.aris.feature.ArisDropdownFeatureButton;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class ArisDropdownWindow extends DropdownWindow {
    public ArisDropdownWindow(DropdownTheme theme, String name, float x, float y, float width, float maxHeight) {
        super(theme, name, x, y, width, maxHeight);
        setColor(0xff202020);
    }

    @Override
    public void init() {
        int i = 0;
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (feature.getCategory().name().equalsIgnoreCase(getName())) {
                ArisDropdownFeatureButton dropdownFeatureButton = new ArisDropdownFeatureButton(this, feature, getX() + getTheme().getButtonWidthOffset(), getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (i * (getTheme().getButtonSize() + getTheme().getButtonOffset())), getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize());
                getButtons().add(dropdownFeatureButton);
                this.setHeight(this.getHeight() + getTheme().getButtonSize() + getTheme().getBottomOffset());
                i++;
            }
        }
        this.setPrevHeight(getHeight());
        this.setScrollbar(new Scrollbar(getX() + getWidth() - 1, getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset(), 2, getHeight() - getTheme().getResizeBoxSize() - getTheme().getTopBarSize() - getTheme().getTopBarOffset(), getHeight() - getTheme().getTopBarSize() - getTheme().getTopBarOffset() * 2, getHeight(), 0xffffffff));
        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + (isOpen() ? getHeight() : getTheme().getTopBarSize()), 0xff000000, getColor());
        FontHelper.INSTANCE.drawCenteredString(matrixStack, getName(), getX() + getWidth() / 2.f, getY() + (getTheme().getTopBarSize() / 2.f - 4), -1);
        if (isOpen()) {
            Scissor.INSTANCE.cut((int) getX(), (int) getY() + (int) getTheme().getTopBarSize() + (int) getTheme().getTopBarOffset(), (int) getWidth(), (int) getHeight() - (int) getTheme().getTopBarSize() - (int) getTheme().getTopBarOffset());
            getButtons().forEach(dropdownButton -> {
                dropdownButton.render(matrixStack);
            });
            Scissor.INSTANCE.seal();
            getScrollbar().render(matrixStack);
            Render2DHelper.INSTANCE.fill(matrixStack, getX() + getWidth() - getTheme().getResizeBoxSize(), getY() + getMaxHeight() - getTheme().getResizeBoxSize(), getX() + getWidth() + getTheme().getResizeBoxSize(), getY() + getMaxHeight() + getTheme().getResizeBoxSize(), getColor());
        }
        super.render(matrixStack);
    }
}
