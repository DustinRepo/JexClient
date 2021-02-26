package me.dustin.jex.helper.math;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.Gui;

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
        Gui guiModule = (Gui) Module.get(Gui.class);
        return guiModule.rainbowClientColor ? getRainbowColor() : guiModule.clientColor;
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
