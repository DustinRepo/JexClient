package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;

public class TPSElement extends HudElement {
    public TPSElement(float x, float y, float minWidth, float minHeight) {
        super("TPS", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        String str = String.format("TPS\247f: \2477%.2f", TPSHelper.INSTANCE.getAverageTPS());
        if (getHud().instantTPS) {
            str += String.format("\247r Instant\247f: \2477%.2f", TPSHelper.INSTANCE.getTPS(2));
        }
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public boolean isVisible() {
        return getHud().info && getHud().tps;
    }
}
