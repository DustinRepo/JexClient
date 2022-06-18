package me.dustin.jex.gui.jexgui.impl;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.gui.jexgui.impl.properties.*;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;

public class JexPropertyButton extends Button {
    private final Property<?> genericProperty;
    private final ArrayList<Button> buttonsList;
    private JexPropertyButton parentButton;
    private JexPropertyButton masterButton;
    private float buttonsHeight;
    private final int color;

    public JexPropertyButton(Property<?> property, float x, float y, float width, float height, ArrayList<Button> buttonsList, int color) {
        super(property.getName(), x, y, width, height, null);
        this.genericProperty = property;
        this.buttonsList = buttonsList;
        this.color = color;
        setBackgroundColor(0xa0000000);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (hasChild())
            FontHelper.INSTANCE.drawWithShadow(matrixStack, isOpen() ? "-" : "+", getX() + getWidth() - 15, getY() + 4, !isOpen() ? -1 : getColor());
        getChildren().forEach(button -> button.render(matrixStack));
    }

    public Property<?> getGenericProperty() {
        return genericProperty;
    }

    public void tick() {
        this.getChildren().forEach(button -> {
            if (button instanceof JexPropertyButton jexPropertyButton)
                jexPropertyButton.tick();
        });
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (int_1 == 1 && isHovered()) {
            if (hasChild()) {
                this.setOpen(!this.isOpen());
                if (this.isOpen())
                    this.open();
                else
                    this.close();
            }
        }
        this.getChildren().forEach(button -> {
            button.click(double_1, double_2, int_1);
        });
    }

    private boolean hasChild() {
        for (Property<?> property : this.getGenericProperty().getChildren()) {
            if (property.getDepends() != null) {
                if (property.passes())
                    return true;
            } else {
                return true;
            }
        }
        return false;
    }

    public void open() {
        buttonsHeight = 0;
        getGenericProperty().getChildren().forEach(option -> {
            Property<?> property = (Property<?>)option;
            JexPropertyButton optionButton = null;

            if (property.getDefaultValue() instanceof Boolean) {
                optionButton = new JexBooleanPropertyButton((Property<Boolean>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
            } else if (property.getDefaultValue() instanceof Float) {
                optionButton = new JexFloatPropertyButton((Property<Float>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
            } else if (property.getDefaultValue() instanceof Double) {
                optionButton = new JexDoublePropertyButton((Property<Double>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
            } else if (property.getDefaultValue() instanceof Integer) {
                if (property.isKeybind())
                    optionButton = new JexKeybindPropertyButton((Property<Integer>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
                else
                    optionButton = new JexIntegerPropertyButton((Property<Integer>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
            } else if (property.getDefaultValue() instanceof Long) {
                optionButton = new JexLongPropertyButton((Property<Long>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
            } else if (property.getDefaultValue() instanceof Color) {
                optionButton = new JexColorPropertyButton((Property<Color>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 100, buttonsList, getColor());
            } else if (property.getDefaultValue() instanceof Enum<?>) {
                optionButton = new JexEnumPropertyButton((Property<Enum<?>>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
            } else if (property.getDefaultValue() instanceof String) {
                optionButton = new JexStringPropertyButton((Property<String>) property, getX() + 1, getY() + getHeight() + buttonsHeight, getWidth() - 2, 25, buttonsList, getColor());
            }

            if (optionButton == null)
                return;

            optionButton.masterButton = this.masterButton == null ? this : this.masterButton;
            optionButton.parentButton = this;

            if (optionButton.getGenericProperty().getDepends() != null) {
                if (optionButton.getGenericProperty().getDepends().test(optionButton.getGenericProperty().getParent())) {
                    this.getChildren().add(optionButton);
                    buttonsHeight += optionButton.getHeight();
                }
            } else {
                this.getChildren().add(optionButton);
                buttonsHeight += optionButton.getHeight();
            }
        });
        allButtonsAfter().forEach(button -> button.move(0, buttonsHeight));
    }

    public void close() {
        this.getChildren().forEach(button -> {
            if (button instanceof JexPropertyButton) {
                if (button.isOpen())
                    ((JexPropertyButton) button).close();
            }
        });
        allButtonsAfter().forEach(button -> button.move(0, -buttonsHeight));
        this.getChildren().clear();
        this.setOpen(false);
    }

    public ArrayList<Button> allButtonsAfter() {
        ArrayList<Button> buttons = new ArrayList<>();

        if (parentButton != null)
            parentButton.getChildren().forEach(button -> {
                if (parentButton.getChildren().indexOf(button) > parentButton.getChildren().indexOf(this)) {
                    buttons.add(button);
                    addAllChildren(buttons, button);
                }
            });
        buttons.addAll(allButtonsAfter(masterButton != null ? masterButton : parentButton != null ? parentButton : this));
        return buttons;
    }

    public ArrayList<Button> allButtonsAfter(Button button1) {
        ArrayList<Button> buttons = new ArrayList<>();
        for (Button button : buttonsList) {
            if (buttonsList.indexOf(button) > buttonsList.indexOf(button1) && button.isVisible()) {
                buttons.add(button);
                buttons = addAllChildren(buttons, button);
            }
        }
        return buttons;
    }

    public int getColor() {
        return color;
    }
}
