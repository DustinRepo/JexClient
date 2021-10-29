package me.dustin.jex.gui.click.navigator.impl;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.gui.click.window.impl.Window;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.helper.file.files.FeatureFile;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class NavigatorFeatureVisibleButton extends Button {
    private Feature feature;
    public NavigatorFeatureVisibleButton(Feature feature, float x, float y, float width, float height) {
        super(null, "", x, y, width, height, null);
        this.feature = feature;
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, this.getX() + 2, this.getY() + 2, this.getX() + this.getHeight() - 4, this.getY() + this.getHeight() - 4, 0xff656565, 0x00ffffff);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        if (feature.isVisible())
            Render2DHelper.INSTANCE.drawCheckmark(matrixStack, this.getX() + 2, this.getY() + 2, ColorHelper.INSTANCE.getClientColor());
        FontHelper.INSTANCE.draw(matrixStack, "Visible", this.getX() + 14, this.getY() + (this.getHeight() / 2.f) - 4.f, -1);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0) {
            feature.setVisible(!feature.isVisible());
            if (JexClient.INSTANCE.isAutoSaveEnabled())
                FeatureFile.write();
        }
    }
}