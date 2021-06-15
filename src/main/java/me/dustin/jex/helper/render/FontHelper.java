package me.dustin.jex.helper.render;

import me.dustin.jex.feature.impl.render.CustomFont;
import me.dustin.jex.font.NahrFont;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public enum FontHelper {
    INSTANCE;
    private NahrFont verdana = new NahrFont("Verdana", 18, 1.2f);

    public float getStringWidth(String string) {
        if (CustomFont.INSTANCE.getState())
            return verdana.getStringWidth(verdana.stripControlCodes(string));
        else
            return Wrapper.INSTANCE.getTextRenderer().getWidth(string);
    }

    public float getStringWidth(String string, boolean customFont) {
        if (customFont)
            return verdana.getStringWidth(verdana.stripControlCodes(string));
        else
            return Wrapper.INSTANCE.getTextRenderer().getWidth(string);
    }

    public float getStringHeight(String string, boolean customFont) {
        if (customFont)
            return verdana.getStringHeight(verdana.stripControlCodes(string));
        else
            return Wrapper.INSTANCE.getTextRenderer().fontHeight;
    }

    public float getStringWidth(Text string) {
        return Wrapper.INSTANCE.getTextRenderer().getWidth(string);
    }

    public void drawWithShadow(MatrixStack matrixStack, String text, float x, float y, int color, boolean customFont) {
        if (CustomFont.INSTANCE.getState() || customFont) {
            verdana.drawString(matrixStack, text, x - 1f, y - 4.5f, NahrFont.FontType.SHADOW_THIN, color);
        } else {
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, fix(text), x + 0.5f, y + 0.5f, 0xff000000);
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, text, x, y, color);
        }
    }

    public void drawWithShadow(MatrixStack matrixStack, String text, float x, float y, int color) {
        drawWithShadow(matrixStack, text, x, y, color, CustomFont.INSTANCE.getState());
    }

    public void draw(MatrixStack matrixStack, String text, float x, float y, int color) {
        draw(matrixStack, text, x, y, color, CustomFont.INSTANCE.getState());
    }

    public void draw(MatrixStack matrixStack, String text, float x, float y, int color, boolean customFont) {
        if (CustomFont.INSTANCE.getState() || customFont) {
            verdana.drawString(matrixStack, text, x - 1f, y - 4.5f, NahrFont.FontType.NORMAL, color);
        } else {
            Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, text, x, y, color);
        }
    }

    public void drawCenteredString(MatrixStack matrixStack, String string, float x, float y, int color, boolean customFont) {
        float newX = x - (getStringWidth(string, customFont || CustomFont.INSTANCE.getState()) / 2);
        drawWithShadow(matrixStack, string, newX, y, color, customFont || CustomFont.INSTANCE.getState());
    }

    public void drawCenteredString(MatrixStack matrixStack, String string, float x, float y, int color) {
        float newX = x - (getStringWidth(string, CustomFont.INSTANCE.getState()) / 2);
        drawWithShadow(matrixStack, string, newX, y, color);
    }

    public void drawWithShadow(MatrixStack matrixStack, Text text, float x, float y, int color) {
        String s = text.getString();
        draw(matrixStack, s, x + 0.5f, y + 0.5f, 0xff000000);
        draw(matrixStack, s, x, y, color);
    }

    public void draw(MatrixStack matrixStack, Text text, float x, float y, int color) {
        Wrapper.INSTANCE.getTextRenderer().draw(matrixStack, text, x, y, color);
    }

    public void drawCenteredString(MatrixStack matrixStack, Text string, float x, float y, int color) {
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

    public NahrFont getClientFont() {
        return verdana;
    }
}
