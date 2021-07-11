package me.dustin.jex.helper.math;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Hud;

import java.awt.*;

public enum ColorHelper {
    INSTANCE;

    private int clientColor = 0xff15f4ee;

    private int rainbowHue = 0;

    public Color getColorViaHue(float hue) {
        float v = 100.0F;
        if (hue > 270.0F) {
            hue = 0.0F;
        }
        return Color.getHSBColor(hue / 270.0F, 1, v / 100.0F);
    }

    public Color getColorViaHue(float hue, float s) {
        float v = 100.0f;
        if (hue > 270.0F) {
            hue = 0.0F;
        }
        return Color.getHSBColor(hue / 270.0F, s, v / 100.0F);
    }

    public Color getColorViaHue(float hue, float s, float b) {
        if (hue > 270.0F) {
            hue = 0.0F;
        }
        return Color.getHSBColor(hue / 270.0F, s, b);
    }

    public int getClientColor() {
        Hud hud = (Hud) Feature.get(Hud.class);
        return hud.rainbowClientColor ? getRainbowColor() : hud.clientColor;
    }

    public Color getColor(int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        return new Color(red, green, blue, alpha);
    }

    public void setClientColor(int clientColor) {
        this.clientColor = clientColor;
    }

    public int getRainbowColor() {
        return getColorViaHue(rainbowHue).getRGB();
    }

    @EventListener(events = {EventTick.class})
    public void runMethod(EventTick eventTick) {
        this.rainbowHue++;
        if (rainbowHue > 270) {
            rainbowHue -= 270;
        }

    }
}
