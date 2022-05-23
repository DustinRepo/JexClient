package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.tab.TabGui;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

public class TabGuiElement extends HudElement {
    public TabGuiElement(float x, float y, float minWidth, float minHeight) {
        super("TabGui", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen) {
            if (isHovered())
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getName(), getX() + (getWidth() / 2.f), getY() - 10, -1);
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY() - 1, getX() + getWidth(), getY() + getHeight(), isHovered() ? ColorHelper.INSTANCE.getClientColor() : 0xff696969, 0x00000000, 1);
        }
        handleElement();
        TabGui.INSTANCE.setHoverBar(getHud().hoverBarProperty.value());
        TabGui.INSTANCE.draw(matrixStack, this.getX(), this.getY(), getHud().tabGuiWidthProperty.value(), getHud().buttonHeightProperty.value());
        this.setWidth(getHud().tabGuiWidthProperty.value());
        this.setHeight(Category.values().size() * getHud().buttonHeightProperty.value());
    }

    @Override
    public boolean isVisible() {
        return getHud().tabGuiProperty.value();
    }
}
