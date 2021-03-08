package me.dustin.jex.gui.account.impl;

import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public class AccountButton {

    private float x, y, width, height;
    private MinecraftAccount account;
    private boolean isSelected;

    private UUID uuid;
    private Identifier id;

    public AccountButton(MinecraftAccount account, float x, float y) {
        this.account = account;
        this.x = x;
        this.y = y;
        this.width = 148;
        this.height = 40;
    }

    public void draw(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), !isSelected() ? 0x85000000 : ColorHelper.INSTANCE.getClientColor(), 0x45000000, 1);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x30ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, account.getUsername(), getX() + 40, getY() + 5, -1);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getAccount().isCracked() ? "Cracked" : "Premium", getX() + 40, getY() + 15, 0xff676767);
        String pword = "";
        for (int i = 0; i < getAccount().getPassword().length(); i++) {
            pword += "*";
        }
        FontHelper.INSTANCE.drawWithShadow(matrixStack, pword, getX() + 40, getY() + 25, 0xff676767);
        if (uuid == null) {
            uuid = PlayerHelper.INSTANCE.getUUID(account.getUsername());
            if (uuid != null) {
                String avatarURL = "https://crafatar.com/avatars/" + uuid.toString().replace("-", "") + "?size=64&overlay";
                try {
                    BufferedImage image = ImageIO.read(new URL(avatarURL));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", bos);

                    ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());

                    NativeImage nativeImage = NativeImage.read(bais);
                    applyTexture(new Identifier("jex", "avatar/" + account.getUsername().toLowerCase()), nativeImage);
                    id = new Identifier("jex", "avatar/" + account.getUsername().toLowerCase());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (id != null){
            Wrapper.INSTANCE.getMinecraft().getTextureManager().bindTexture(id);
            DrawableHelper.drawTexture(matrixStack, (int)this.getX() + 4, (int)this.getY() + 4, 0, 0, 32, 32, 32, 32);
        }
    }

    private static void applyTexture(Identifier identifier, NativeImage nativeImage) {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(nativeImage)));
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
