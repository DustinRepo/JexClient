package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.tab.TabGui;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screens.ChatScreen;

public class TabGuiElement extends HudElement {
    public TabGuiElement(float x, float y, float minWidth, float minHeight) {
        super("TabGui", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        if (Wrapper.INSTANCE.getMinecraft().screen instanceof ChatScreen) {
            if (isHovered())
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getName(), getX() + (getWidth() / 2.f), getY() - 10, -1);
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY() - 1, getX() + getWidth(), getY() + getHeight(), isHovered() ? ColorHelper.INSTANCE.getClientColor() : 0xff696969, 0x00000000, 1);
        }
        handleElement();
        TabGui.INSTANCE.setHoverBar(getHud().hoverBar);
        TabGui.INSTANCE.draw(matrixStack, this.getX(), this.getY(), getHud().tabGuiWidth, getHud().buttonHeight);
        this.setWidth(getHud().tabGuiWidth);
        this.setHeight(Feature.Category.values().length * getHud().buttonHeight);
    }

    @Override
    public boolean isVisible() {
        return getHud().tabGui;
    }
}
