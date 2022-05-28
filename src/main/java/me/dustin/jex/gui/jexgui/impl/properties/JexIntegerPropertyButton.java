package me.dustin.jex.gui.jexgui.impl.properties;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.impl.JexPropertyButton;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class JexIntegerPropertyButton extends JexPropertyButton {
    private final Property<Integer> integerProperty;
    private boolean isSliding;
    public JexIntegerPropertyButton(Property<Integer> integerProperty, float x, float y, float width, float height, ArrayList<JexPropertyButton> buttonList, int color) {
        super(integerProperty, x, y, width, height, buttonList, color);
        this.integerProperty = integerProperty;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getBackgroundColor());
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        float startV = (float)(getIntegerProperty().value() - getIntegerProperty().getMin());
        float perc = (startV / (getIntegerProperty().getMax() - getIntegerProperty().getMin()));
        float pos = perc * (this.getWidth());

        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY() + this.getHeight() - 4, this.getX() + this.getWidth(), this.getY() + this.getHeight() - 2, 0xff696969);
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY() + this.getHeight() - 4, this.getX() + pos, this.getY() + this.getHeight() - 2, getColor());
        FontHelper.INSTANCE.drawCenteredString(matrixStack, getIntegerProperty().getName() + ": " + getIntegerProperty().value(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        super.render(matrixStack);
    }

    public void handleSlider() {
        float position = MouseHelper.INSTANCE.getMouseX() - this.getX();
        float percent = MathHelper.clamp(position / this.getWidth(), 0, 1);
        int increment = (int)getIntegerProperty().getInc();
        int value = (int) (getIntegerProperty().getMin() + (int) (percent * (getIntegerProperty().getMax() - getIntegerProperty().getMin())));
        integerProperty.setValue((int) ((int) Math.round(value * (1.0D / increment)) / (1.0D / increment)));
    }

    @Override
    public void tick() {
        if (isSliding) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
                isSliding = false;
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    ConfigManager.INSTANCE.get(FeatureFile.class).write();
                return;
            }
            handleSlider();
        }
        super.tick();
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0) {
            isSliding = true;
        }
        super.click(double_1, double_2, int_1);
    }

    public Property<Integer> getIntegerProperty() {
        return this.integerProperty;
    }
}
