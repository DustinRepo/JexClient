package me.dustin.jex.feature.mod.impl.render.hud.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PlayerFaceElement extends HudElement {
    public PlayerFaceElement(float x, float y, float minWidth, float minHeight) {
        super("Face", x, y, minWidth, minHeight);
    }

    @Override
    public void render(PoseStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        if (Wrapper.INSTANCE.getMinecraft().getConnection() != null && Wrapper.INSTANCE.getMinecraft().getConnection().getPlayerInfo(Wrapper.INSTANCE.getMinecraft().getUser().getGameProfile().getId()) != null) {
            PlayerInfo playerListEntry = Wrapper.INSTANCE.getMinecraft().getConnection().getPlayerInfo(Wrapper.INSTANCE.getMinecraft().getUser().getGameProfile().getId());
            if (playerListEntry != null)
                Render2DHelper.INSTANCE.drawFace(matrixStack, getX() + 1, getY() + 1, 4, playerListEntry.getSkinLocation());
        }
    }

    @Override
    public boolean isVisible() {
        return getHud().drawFace;
    }
}
