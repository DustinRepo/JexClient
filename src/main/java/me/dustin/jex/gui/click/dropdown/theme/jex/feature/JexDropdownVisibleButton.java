package me.dustin.jex.gui.click.dropdown.theme.jex.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.EventManager;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownVisibleButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class JexDropdownVisibleButton extends DropdownVisibleButton {
    public JexDropdownVisibleButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getName(), getX() + 2, getY() + 2, EventManager.isRegistered(this) ? 0xff00ff00 : -1);
    }
}
