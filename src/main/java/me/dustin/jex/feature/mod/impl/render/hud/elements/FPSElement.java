package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.TPSHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;

public class FPSElement extends HudElement {
    public FPSElement(float x, float y, float minWidth, float minHeight) {
        super("FPS", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        String str = String.format("FPS\247f: \2477%s", Wrapper.INSTANCE.getMinecraft().fpsString.split(" ")[0]);
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public boolean isVisible() {
        return getHud().info && getHud().fps;
    }
}
