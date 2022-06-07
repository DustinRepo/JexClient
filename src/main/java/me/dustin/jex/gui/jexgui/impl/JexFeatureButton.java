package me.dustin.jex.gui.jexgui.impl;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import net.minecraft.client.util.math.MatrixStack;

public class JexFeatureButton extends Button {
    private final Feature feature;
    public JexFeatureButton(Feature feature, float x, float y, float width, float height, ButtonListener listener) {
        super(feature.getName(), x, y, width, height, listener);
        this.feature = feature;
        setBackgroundColor(0xa0000000);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        setTextColor(feature.getState() ? getFeature().getCategory().color() : 0xff676767);
        super.render(matrixStack);
    }

    public Feature getFeature() {
        return this.feature;
    }
}
