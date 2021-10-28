package me.dustin.jex.gui.account.altening;

import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.network.login.thealtening.TheAlteningHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class AlteningAccountButton {

    private float x, y, width, height;
    private TheAlteningHelper.TheAlteningAccount account;
    private boolean isSelected;

    private UUID uuid;

    public AlteningAccountButton(TheAlteningHelper.TheAlteningAccount account, float x, float y) {
        this.account = account;
        this.x = x;
        this.y = y;
        this.width = 148;
        this.height = 30;
        uuid = PlayerHelper.INSTANCE.getUUID(account.username);
    }

    public void draw(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), !isSelected() ? 0x85000000 : ColorHelper.INSTANCE.getClientColor(), 0x45000000, 1);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x30ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, Formatting.AQUA + account.username, getX() + 24, getY() + 2, -1);
        String token = account.token;
        if (!isSelected()) {
            token = "*****-*****@alt.com";
        }
        FontHelper.INSTANCE.drawWithShadow(matrixStack, Formatting.GREEN + token, getX() + 24, getY() + 12, -1);
        Render2DHelper.INSTANCE.bindTexture(TheAlteningHelper.INSTANCE.getSkin(account));
        Render2DHelper.INSTANCE.drawTexture(matrixStack, this.getX() + 4, this.getY() + 2, 0, 0, 16, 25, 16, 25);

    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(x, y, width, height);
    }

    public TheAlteningHelper.TheAlteningAccount getAccount() {
        return account;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
