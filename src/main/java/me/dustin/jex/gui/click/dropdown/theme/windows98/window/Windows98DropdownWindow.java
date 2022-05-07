package me.dustin.jex.gui.click.dropdown.theme.windows98.window;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.aris.feature.ArisDropdownFeatureButton;
import me.dustin.jex.gui.click.dropdown.theme.windows98.feature.Windows98DropdownFeatureButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;

public class Windows98DropdownWindow extends DropdownWindow {
    public Windows98DropdownWindow(DropdownTheme theme, String name, float x, float y, float width, float maxHeight) {
        super(theme, name, x, y, width, maxHeight);
        setColor(0xff131174);
    }

    @Override
    public void init() {
        int i = 0;
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (feature.getFeatureCategory().name().equalsIgnoreCase(getName())) {
                Windows98DropdownFeatureButton dropdownFeatureButton = new Windows98DropdownFeatureButton(this, feature, getX() + getTheme().getButtonWidthOffset(), getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (i * (getTheme().getButtonSize() + getTheme().getButtonOffset())), getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize());
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
    public void render(PoseStack matrixStack) {
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY(), getX() + getWidth(), getY() + (isOpen() ? getHeight() : getTheme().getTopBarSize()), 0xffbfbfbf, 0x00ffffff, 1);
        Render2DHelper.INSTANCE.gradientSidewaysFill(matrixStack, getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getTheme().getTopBarSize(), this == getTheme().getTopWindow() ? getColor() : 0xff6e6d6c, this == getTheme().getTopWindow() ? 0xff8cb9ff : 0xffbebebe);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getName(), getX() + 2, getY() + (getTheme().getTopBarSize() / 2.f - 4), -1);
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
