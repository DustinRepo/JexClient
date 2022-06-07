package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.math.MatrixStack;

public class PingElement extends HudElement {
    public PingElement(float x, float y, float minWidth, float minHeight) {
        super("Ping", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        String str = String.format("Ping\247f: \2477%d", Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(Wrapper.INSTANCE.getLocalPlayer().getUuid()) == null ? 0 : Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(Wrapper.INSTANCE.getLocalPlayer().getUuid()).getLatency());
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public boolean isVisible() {
        return getHud().infoProperty.value() && getHud().pingProperty.value();
    }
}
