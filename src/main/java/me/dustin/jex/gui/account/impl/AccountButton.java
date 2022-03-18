package me.dustin.jex.gui.account.impl;

import me.dustin.jex.JexClient;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.UUID;

public class AccountButton {

    private float x, y, width, height;
    private MinecraftAccount account;
    private boolean isSelected;

    private UUID uuid;

    public AccountButton(MinecraftAccount account, float x, float y) {
        this.account = account;
        this.x = x;
        this.y = y;
        this.width = 148;
        this.height = 40;
        uuid = PlayerHelper.INSTANCE.getUUID(account.getUsername());
    }

    public void draw(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), !isSelected() ? 0x85000000 : ColorHelper.INSTANCE.getClientColor(), 0x45000000, 1);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x30ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, account.getUsername(), getX() + 40, getY() + 5, -1);
        String pword = "";
        if (account instanceof MinecraftAccount.MojangAccount mojangAccount) {
            FontHelper.INSTANCE.drawWithShadow(matrixStack, mojangAccount.isCracked() ? "Cracked" : "Mojang Account", getX() + 40, getY() + 15, mojangAccount.isCracked() ? 0xffff0000 : 0xffdd7000);
            if (mojangAccount.getPassword() == null) {
                pword = "ERROR READING FILE";
            } else
                for (int i = 0; i < mojangAccount.getPassword().length(); i++) {
                    pword += "*";
                }
            if (uuid == null)
                uuid = PlayerHelper.INSTANCE.getUUID(account.getUsername());
        } else {
            FontHelper.INSTANCE.drawWithShadow(matrixStack, "Microsoft Account", getX() + 40, getY() + 15, 0xff00ff00);
        }
        if (this.getAccount().getUsername().equalsIgnoreCase(Wrapper.INSTANCE.getMinecraft().getSession().getUsername())) {
            Render2DHelper.INSTANCE.drawItem(new ItemStack(Items.EMERALD), getX() + getWidth() - 20, getY() + getHeight() - 20);
        }
        FontHelper.INSTANCE.drawWithShadow(matrixStack, pword, getX() + 40, getY() + 25, 0xff676767);
        Render2DHelper.INSTANCE.drawFace(matrixStack, this.getX() + 4, this.getY() + 4, 4, MCAPIHelper.INSTANCE.getPlayerSkin(uuid));
    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(x, y, width, height);
    }

    public MinecraftAccount getAccount() {
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
