package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ArrayListElement extends HudElement {
    private ArrayList<Feature> mods = new ArrayList<>();

    public ArrayListElement(float x, float y, float minWidth, float minHeight) {
        super("Array List", x, y, minWidth, minHeight);
    }
    private int count;
    private int rainbowScroll = 0;
    @Override
    public void render(MatrixStack matrixStack) {
        if (!isVisible())
            return;
        super.render(matrixStack);
        if (mods.isEmpty())
            mods.addAll(FeatureManager.INSTANCE.getFeatures());

        int num = count;
        count = 0;

        float longest = 0;

        for (Feature mod : mods) {
            float strWidth = FontHelper.INSTANCE.getStringWidth(mod.getDisplayName());
            if (strWidth > longest)
                longest = strWidth;
            float x = isLeftSide() ? getX() + 3 : getX() + getWidth() - strWidth - 1;
            float y = isTopSide() ? getY() + 3 + (count * 10) : getY() + getHeight() - 10 - (count * 10);

            int color = getRainbowColor(count, num);
            if (getHud().colorMode.equalsIgnoreCase("Category"))
                color = getHud().getCategoryColor(mod.getFeatureCategory());
            if (getHud().colorMode.equalsIgnoreCase("Client Color"))
                color = ColorHelper.INSTANCE.getClientColor();
            if (mod.isVisible() && mod.getState()) {
                FontHelper.INSTANCE.drawWithShadow(matrixStack, mod.getDisplayName(), x, y, color);
                count++;
            }
        }
        this.setHeight(3 + (count * 10));
        this.setWidth(longest + 7);
    }

    @Override
    public boolean isVisible() {
        return getHud().showArrayList;
    }

    @Override
    public void tick() {
        reorderArrayList(mods);
        rainbowScroll += getHud().rainbowSpeed;
        super.tick();
    }

    public int getRainbowColor(int count, int max) {
        if (max == 0)
            max = 1;
        int inc = 270 / max;
        int hue = rainbowScroll + count * inc;
        return ColorHelper.INSTANCE.getColorViaHue(hue % 270, getHud().rainbowSaturation).getRGB();
    }


    private void reorderArrayList(ArrayList<Feature> mods) {
        Collections.sort(mods, (mod, mod1) -> {
            String name1 = mod.getDisplayName();
            String name2 = mod1.getDisplayName();
            if (FontHelper.INSTANCE.getStringWidth(name1) > FontHelper.INSTANCE.getStringWidth(name2)) {
                return -1;
            }
            if (FontHelper.INSTANCE.getStringWidth(name1) < FontHelper.INSTANCE.getStringWidth(name2)) {
                return 1;
            }
            return 0;
        });
    }
}
