package me.dustin.jex.helper.render.font;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.impl.render.CustomFont;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.chat.Component;

public enum FontHelper {
    INSTANCE;
    private NahrFont clientFont;

    public float getStringWidth(String string) {
        if (CustomFont.INSTANCE.getState())
            return clientFont.getStringWidth(clientFont.stripControlCodes(string));
        else
            return Wrapper.INSTANCE.getTextRenderer().width(string);
    }

    public float getStringWidth(String string, boolean customFont) {
        if (customFont)
            return clientFont.getStringWidth(clientFont.stripControlCodes(string));
        else
            return Wrapper.INSTANCE.getTextRenderer().width(string);
    }

    public float getStringHeight(String string, boolean customFont) {
        if (customFont)
            return clientFont.getStringHeight(clientFont.stripControlCodes(string));
        else
            return Wrapper.INSTANCE.getTextRenderer().lineHeight;
    }

    public float getStringWidth(Component string) {
        return Wrapper.INSTANCE.getTextRenderer().width(string);
    }

    public void drawWithShadow(PoseStack matrixStack, String text, float x, float y, int color, boolean customFont) {
        if (CustomFont.INSTANCE.getState() || customFont) {
            clientFont.drawString(matrixStack, text, x + CustomFont.INSTANCE.xOffset, y + CustomFont.INSTANCE.yOffset, CustomFont.INSTANCE.textShadows ? NahrFont.FontType.SHADOW_THIN : NahrFont.FontType.NORMAL, color);
        } else {
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, fix(text), x + 0.5f, y + 0.5f, 0xff000000);
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, text, x, y, color);
        }
    }

    public void drawWithShadow(PoseStack matrixStack, String text, float x, float y, int color) {
        drawWithShadow(matrixStack, text, x, y, color, CustomFont.INSTANCE.getState());
    }

    public void draw(PoseStack matrixStack, String text, float x, float y, int color) {
        draw(matrixStack, text, x, y, color, CustomFont.INSTANCE.getState());
    }

    public void draw(PoseStack matrixStack, String text, float x, float y, int color, boolean customFont) {
        if (CustomFont.INSTANCE.getState() || customFont) {
            clientFont.drawString(matrixStack, text, x + CustomFont.INSTANCE.xOffset, y + CustomFont.INSTANCE.yOffset, NahrFont.FontType.NORMAL, color);
        } else {
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, text, x, y, color);
        }
    }

    public void drawCenteredString(PoseStack matrixStack, String string, float x, float y, int color, boolean customFont) {
        float newX = x - ((getStringWidth(string, CustomFont.INSTANCE.getState() || customFont) + (CustomFont.INSTANCE.getState() || customFont ? CustomFont.INSTANCE.xOffset : 0)) / 2);
        if (CustomFont.INSTANCE.getState() || customFont) {
            clientFont.drawString(matrixStack, string, newX, y + CustomFont.INSTANCE.yOffset, CustomFont.INSTANCE.textShadows ? NahrFont.FontType.SHADOW_THIN : NahrFont.FontType.NORMAL, color);
        } else {
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, fix(string), newX + 0.5f, y + 0.5f, 0xff000000);
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, string, newX, y, color);
        }
    }

    public void drawCenteredString(PoseStack matrixStack, String string, float x, float y, int color) {
        float newX = x - getStringWidth(string) / 2;
        if (CustomFont.INSTANCE.getState()) {
            clientFont.drawString(matrixStack, string, newX, y + CustomFont.INSTANCE.yOffset, CustomFont.INSTANCE.textShadows ? NahrFont.FontType.SHADOW_THIN : NahrFont.FontType.NORMAL, color);
        } else {
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, fix(string), newX + 0.5f, y + 0.5f, 0xff000000);
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, string, newX, y, color);
        }
    }

    public void drawWithShadow(PoseStack matrixStack, Component text, float x, float y, int color) {
        String s = text.getString();
        draw(matrixStack, s, x + 0.5f, y + 0.5f, 0xff000000);
        draw(matrixStack, s, x, y, color);
    }

    public void draw(PoseStack matrixStack, Component text, float x, float y, int color) {
        Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, text, x, y, color);
    }

    public void drawCenteredString(PoseStack matrixStack, Component string, float x, float y, int color) {
        float newX = x - (getStringWidth(string) / 2);

        drawWithShadow(matrixStack, string, newX, y, color);
    }

    public String fix(String s) {
        if (s == null || s.isEmpty())
            return s;
        for (int i = 0; i < 9; i++) {
            if (s.contains("\247" + i))
                s = s.replace("\247" + i, "");
        }
        return s.replace("\247a", "").replace("\247b", "").replace("\247c", "").replace("\247d", "").replace("\247e", "").replace("\247f", "").replace("\247g", "");
    }

    public boolean setClientFont(NahrFont font) {
        try {
            this.clientFont = font;
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public NahrFont getClientFont() {
        return clientFont;
    }
}
