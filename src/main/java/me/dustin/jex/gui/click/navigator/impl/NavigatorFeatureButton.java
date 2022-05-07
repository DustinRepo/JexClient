package me.dustin.jex.gui.click.navigator.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;

public class NavigatorFeatureButton extends Button {

    private Feature feature;

    public NavigatorFeatureButton(Feature feature, float x, float y, float width, float height, ButtonListener listener) {
        super(feature.getName(), x, y, width, height, listener);
        this.feature = feature;
    }

    @Override
    public void render(PoseStack matrixStack) {
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x99656565, feature.getState() ? ColorHelper.INSTANCE.getClientColor() & 0x70ffffff : 0x70000000);
        if (isHovered())
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x00ffffff, 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, feature.getName(), getX() + 4, getY() + (getHeight() / 2.f) - 4.5f, -1);
    }

    public Feature getFeature() {
        return feature;
    }
}
