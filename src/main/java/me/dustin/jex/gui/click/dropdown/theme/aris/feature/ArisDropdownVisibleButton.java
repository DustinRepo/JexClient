package me.dustin.jex.gui.click.dropdown.theme.aris.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.EventManager;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownVisibleButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class ArisDropdownVisibleButton extends DropdownVisibleButton {
    public ArisDropdownVisibleButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xff000000, getFeature().isVisible() ? 0xff333333 : 0xff202020);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x45000000);
        if (getFeature().isVisible())
            Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX(), getY() + 0.5f, getX() + getWidth() - 0.5f, 0xff4a4a4a);

        FontHelper.INSTANCE.drawCenteredString(matrixStack, "Visible", getX() + getWidth() / 2.f, getY()  + (getHeight() / 2 - 4), -1);
        super.render(matrixStack);
    }
}
