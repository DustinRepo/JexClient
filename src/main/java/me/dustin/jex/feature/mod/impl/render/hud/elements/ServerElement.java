package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.util.math.MatrixStack;

public class ServerElement extends HudElement {
    public ServerElement(float x, float y, float minWidth, float minHeight) {
        super("Server", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!getHud().info || !getHud().serverName)
            return;
        super.render(matrixStack);
        String str = String.format("Server\247f: \2477%s", WorldHelper.INSTANCE.getCurrentServerName() + " " + (Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry() == null ? SharedConstants.getGameVersion().getName() : Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().version.getString()));
        float x = isLeftSide() ? getX() + 2.5f : getX() + getWidth() - 0.5f - FontHelper.INSTANCE.getStringWidth(str);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, str, x, getY() + 1.5f, ColorHelper.INSTANCE.getClientColor());
        this.setWidth(FontHelper.INSTANCE.getStringWidth(str) + 3);
    }

    @Override
    public void click(int mouseX, int mouseY, int mouseButton) {
        if (!getHud().info || !getHud().serverName)
            return;
        super.click(mouseX, mouseY, mouseButton);
    }
}
