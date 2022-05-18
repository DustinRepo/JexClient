package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;

public class PlayerFaceElement extends HudElement {
    public PlayerFaceElement(float x, float y, float minWidth, float minHeight) {
        super("Face", x, y, minWidth, minHeight);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        if (Wrapper.INSTANCE.getMinecraft().getNetworkHandler() != null && Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(Wrapper.INSTANCE.getMinecraft().getSession().getProfile().getId()) != null) {
            PlayerListEntry playerListEntry = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(Wrapper.INSTANCE.getMinecraft().getSession().getProfile().getId());
            if (playerListEntry != null)
                Render2DHelper.INSTANCE.drawFace(matrixStack, getX() + 1, getY() + 1, 4, playerListEntry.getSkinTexture());
        }
    }

    @Override
    public boolean isVisible() {
        return getHud().drawFace;
    }
}
