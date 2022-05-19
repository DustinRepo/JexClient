package me.dustin.jex.feature.mod.impl.render.hud.elements;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import java.util.ArrayList;

public class ArrayListElement extends HudElement {
    private final ArrayList<ArrayListItem> list = new ArrayList<>();

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

        int i = 0;
        for (ArrayListItem arrayListItem : list) {
            float strWidth = FontHelper.INSTANCE.getStringWidth(arrayListItem.name());
            float x = isLeftSide() ? getX() + 3 : getX() + getWidth() - strWidth - 1;
            float y = isTopSide() ? getY() + 3 + (i * 10) : getY() + getHeight() - 10 - (i * 10);

            FontHelper.INSTANCE.drawWithShadow(matrixStack, arrayListItem.name(), x, y, arrayListItem.color());
            i++;
        }
    }

    @Override
    public boolean isVisible() {
        return getHud().showArrayList;
    }

    @Override
    public void tick() {
        list.clear();
        int num = count;
        count = 0;
        float longest = 0;
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            float strWidth = FontHelper.INSTANCE.getStringWidth(feature.getDisplayName());
            if (strWidth > longest)
                longest = strWidth;

            int color = ColorHelper.INSTANCE.getClientColor();
            if (getHud().colorMode.equalsIgnoreCase("Category"))
                color = getHud().getCategoryColor(feature.getCategory());
            else if (getHud().colorMode.equalsIgnoreCase("Rainbow"))
                color = getRainbowColor(count, num);
            if (feature.isVisible() && feature.getState()) {
                list.add(new ArrayListItem(feature.getDisplayName(), color));
                count++;
            }
        }
        this.setHeight(3 + (count * 10));
        this.setWidth(longest + 7);

        reorderArrayList(list);
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


    private void reorderArrayList(ArrayList<ArrayListItem> mods) {
        mods.sort((i, ii) -> {
            String name1 = i.name();
            String name2 = ii.name();
            return Float.compare(FontHelper.INSTANCE.getStringWidth(name2), FontHelper.INSTANCE.getStringWidth(name1));
        });
    }

    public record ArrayListItem(String name, int color){}
}
