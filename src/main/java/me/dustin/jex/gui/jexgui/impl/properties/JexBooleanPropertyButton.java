package me.dustin.jex.gui.jexgui.impl.properties;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.impl.JexPropertyButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class JexBooleanPropertyButton extends JexPropertyButton {
    private final Property<Boolean> booleanProperty;
    private float colorShift;
    public JexBooleanPropertyButton(Property<Boolean> booleanProperty, float x, float y, float width, float height, ArrayList<JexPropertyButton> buttonList, int color) {
        super(booleanProperty, x, y, width, height, buttonList, color);
        this.booleanProperty = booleanProperty;
        this.colorShift = booleanProperty.value() ? 1 : 0;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getBackgroundColor());
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        Render2DHelper.INSTANCE.fill(matrixStack, getX() + 2, getY() + 7, getX() + getHeight(), getY() + getHeight() - 9, ColorHelper.INSTANCE.redGreenShift(colorShift));
        float tWidth = getHeight() - 2 - 10;
        float pos = getX() + 3 + (tWidth * colorShift);

        Render2DHelper.INSTANCE.fill(matrixStack, pos, getY() + 8, pos + 8, getY() + getHeight() - 10, 0xffffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getBooleanProperty().getName(), this.getX() + 28, this.getY() + (this.getHeight() / 2.f) - 4.5f, -1);
        super.render(matrixStack);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0) {
            if (isOpen()) {
                close();
            }
            booleanProperty.setValue(!booleanProperty.value());
            if (JexClient.INSTANCE.isAutoSaveEnabled())
                ConfigManager.INSTANCE.get(FeatureFile.class).write();
        }
        super.click(double_1, double_2, int_1);
    }

    @Override
    public void tick() {
        if (getBooleanProperty().value()) {
            if (colorShift < 1)
                colorShift+=0.2f;
        } else {
            if (colorShift > 0)
                colorShift-=0.2f;
        }
        colorShift = MathHelper.clamp(colorShift, 0, 1);
        super.tick();
    }

    public Property<Boolean> getBooleanProperty() {
        return this.booleanProperty;
    }
}
