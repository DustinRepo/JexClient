package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class WatermarkElement extends HudElement {
    public WatermarkElement(float x, float y, float minWidth, float minHeight) {
        super("Watermark", x, y, minWidth, minHeight);
    }

    private int rot = 0;
    private boolean flipRot;

    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof ChatScreen) {
            if (isHovered())
                FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getName(), getX() + (getWidth() / 2.f), getY() - 10, -1);
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), isHovered() ? ColorHelper.INSTANCE.getClientColor() : 0xff696969, 0x00000000, 1);
        }

        handleElement();
        int x = (int)this.getX() + 17;
        int y = (int)this.getY() + 17;
        matrixStack.push();
        matrixStack.translate(x, y, 0);
        switch (getHud().watermarkMode) {
            case "Static":
                break;
            case "Spin Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, 1.0F), rot, true));
                break;
            case "Flip Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 1F, 0F), rot, true));
                break;
            case "SpinFlip":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.5f, 1F), rot, true));
                break;
        }

        matrixStack.translate(-x, -y, 0);
        float newX = x - (FontHelper.INSTANCE.getStringWidth("Jex") / 2);
        Render2DHelper.INSTANCE.drawFullCircle(x, y, 15, 0x80252525, matrixStack);
        Render2DHelper.INSTANCE.drawArc(x, y, 15, ColorHelper.INSTANCE.getClientColor(), 0, 360, 1, matrixStack);
        FontHelper.INSTANCE.draw(matrixStack, "Jex", newX, y - 9, ColorHelper.INSTANCE.getClientColor());
        matrixStack.scale(0.75f, 0.75f, 1);
        float newX1 = x - (FontHelper.INSTANCE.getStringWidth(JexClient.INSTANCE.getVersion()) / (2 / 0.75f));
        FontHelper.INSTANCE.draw(matrixStack, JexClient.INSTANCE.getVersion(), newX1 / 0.75f, y / 0.75f + 1, ColorHelper.INSTANCE.getClientColor());
        matrixStack.scale(1 / 0.75f, 1 / 0.75f, 1);
        matrixStack.translate(x, y, 0);

        switch (getHud().watermarkMode) {
            case "Static":
                break;
            case "Spin Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, 0.0F, -1.0F), rot, true));
                break;
            case "Flip Only":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, -1F, 0F), rot, true));
                break;
            case "SpinFlip":
                matrixStack.multiply(new Quaternion(new Vec3f(0.0F, -0.5f, -1F), rot, true));
                break;
        }
        matrixStack.pop();
    }

    @Override
    public boolean isVisible() {
        return getHud().watermark;
    }

    @Override
    public void tick() {
        if (getHud().watermark) {
            switch (getHud().watermarkMode) {
                case "Static":
                    break;
                case "SpinFlip":
                case "Spin Only":
                    rot+=2;
                    if (rot > 360)
                        rot -= 360;
                    break;
                case "Flip Only":
                    if (flipRot) {
                        rot-=2;
                        if (rot <= 0)
                            flipRot = false;
                    } else {
                        rot+=2;
                        if (rot >= 90)
                            flipRot = true;
                    }
                    break;
            }
        }
        super.tick();
    }
}
