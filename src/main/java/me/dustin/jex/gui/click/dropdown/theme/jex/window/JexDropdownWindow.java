package me.dustin.jex.gui.click.dropdown.theme.jex.window;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.jex.feature.JexDropdownFeatureButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;

public class JexDropdownWindow extends DropdownWindow {

    public JexDropdownWindow(DropdownTheme theme, String name, float x, float y, float width, float maxHeight) {
        super(theme, name, x, y, width, maxHeight);
            this.setColor(ColorHelper.INSTANCE.getClientColor());
    }

    @Override
    public void init() {
        int i = 0;
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (feature.getFeatureCategory().name().equalsIgnoreCase(getName())) {
                JexDropdownFeatureButton dropdownFeatureButton = new JexDropdownFeatureButton(this, feature, getX() + getTheme().getButtonWidthOffset(), getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (i * (getTheme().getButtonSize() + getTheme().getButtonOffset())), getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize());
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
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getTheme().getTopBarSize(), 0xff303030);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getName(), getX() + 2, getY() + (getTheme().getTopBarSize() / 2.f - 4), -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "!!!" : "...", getX() + getWidth() - 10, getY() + (getTheme().getTopBarSize() / 2.f - 4), isOpen() ? getColor() : -1);
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

    @Override
    public void tick() {
        this.setColor(ColorHelper.INSTANCE.getClientColor());
        super.tick();
    }
}
