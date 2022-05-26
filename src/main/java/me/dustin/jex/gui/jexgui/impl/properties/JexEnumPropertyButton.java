package me.dustin.jex.gui.jexgui.impl.properties;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.impl.JexPropertyButton;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.ArrayList;

public class JexEnumPropertyButton extends JexPropertyButton {
    private final Property<Enum<?>> enumProperty;
    public JexEnumPropertyButton(Property<Enum<?>> enumProperty, float x, float y, float width, float height, ArrayList<JexPropertyButton> buttonList, int color) {
        super(enumProperty, x, y, width, height, buttonList, color);
        this.enumProperty = enumProperty;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getBackgroundColor());
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getEnumProperty().getName(), this.getX() + (this.getWidth() / 2.f), this.getY() + 4, 0xffaaaaaa);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, WordUtils.capitalize(getEnumProperty().value().name().toLowerCase().replace("_", " ")), this.getX() + (this.getWidth() / 2.f), this.getY() + 15, getColor());
        super.render(matrixStack);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0) {
            if (isOpen()) {
                close();
            }
            enumProperty.incrementEnumValue();
            if (JexClient.INSTANCE.isAutoSaveEnabled())
                ConfigManager.INSTANCE.get(FeatureFile.class).write();
        }
        super.click(double_1, double_2, int_1);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public Property<Enum<?>> getEnumProperty() {
        return this.enumProperty;
    }
}
