package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class SaturationElement extends HudElement {
    public SaturationElement(float x, float y, float minWidth, float minHeight) {
        super("Saturation", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!getHud().info || !getHud().saturation)
            return;
        super.render(matrixStack);
        String str = String.format("Saturation\247f: \2477%s", Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getSaturationLevel());
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if (!getHud().info || !getHud().saturation)
            return;
        super.click(mouseX, mouseY, mouseButton);
    }
}
