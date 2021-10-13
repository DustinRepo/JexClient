package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

public class PlayerCountElement extends HudElement {
    public PlayerCountElement(float x, float y, float minWidth, float minHeight) {
        super("Player Count", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!getHud().info || !getHud().playerCount)
            return;
        super.render(matrixStack);
        String str = String.format("Player Count\247f: \2477%d", Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerList() == null ? 0 : Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerList().size());
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if (!getHud().info || !getHud().playerCount)
            return;
        super.click(mouseX, mouseY, mouseButton);
    }
}
