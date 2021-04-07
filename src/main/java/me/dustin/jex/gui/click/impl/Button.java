package me.dustin.jex.gui.click.impl;


import me.dustin.jex.gui.click.listener.ButtonListener;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.Gui;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;

public class Button {

    protected Gui gui;
    private Window window;
    private String name;
    private float x, y, width, height;
    private ButtonListener listener;
    private boolean isOpen;
    private boolean isEnabled;
    private boolean isVisible = true;
    private ArrayList<Button> children = new ArrayList<>();

    public Button(Window window, String name, float x, float y, float width, float height, ButtonListener listener) {
        this.window = window;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.listener = listener;
        this.isEnabled = true;
        gui = (Gui) Module.get(Gui.class);
    }

    public void draw(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, x, y, x + width, y + height, 0x80000000);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getName(), this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2) - 4, isEnabled ? 0xffaaaaaa : 0xff676767);
        if (isHovered() && isEnabled)
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        this.getChildren().forEach(button -> {
            if (button.isVisible())
                button.draw(matrixStack);
        });
    }

    public void click(double double_1, double double_2, int int_1) {
        if (this.isHovered() && isEnabled) {
            if (int_1 == 0) {
                if (this.listener != null) {
                    this.listener.invoke();
                }
            }
            if (int_1 == 1) {
                this.setOpen(!this.isOpen());
            }
            Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private boolean shouldBeSoft() {
        return this.getName().contains("Key:") || this.getName().contains("Visible:");
    }

    public void keyTyped(char typedChar, int keyCode) {
        this.children.forEach(button -> {
            if (button != null) keyTyped(typedChar, keyCode);
        });
    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(x, y, width, height);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public ButtonListener getListener() {
        return listener;
    }

    public void setListener(ButtonListener listener) {
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Window getWindow() {
        return window;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void move(float x, float y) {
        this.setX(this.getX() + x);
        this.setY(this.getY() + y);
    }

    public float getFullHeight(Button b) {
        float height = b.getHeight();
        if (b.isOpen())
            for (Button button : b.getChildren()) {
                height += button.getHeight();
                if (button.hasChildren() && button.isOpen())
                    height += getFullHeight(button);
            }
        return height;
    }

    public ArrayList<Button> allButtonsAfter() {
        ArrayList<Button> buttons = new ArrayList<>();
        for (Button button : getWindow().getButtons()) {
            if (getWindow().getButtons().indexOf(button) > getWindow().getButtons().indexOf(this) && button.isVisible()) {
                buttons.add(button);
                buttons = addAllChildren(buttons, button);
            }
        }
        return buttons;
    }

    public ArrayList<Button> allButtonsAfter(Button button1) {
        ArrayList<Button> buttons = new ArrayList<>();
        for (Button button : getWindow().getButtons()) {
            if (getWindow().getButtons().indexOf(button) > getWindow().getButtons().indexOf(button1) && button.isVisible()) {
                buttons.add(button);
                buttons = addAllChildren(buttons, button);
            }
        }
        return buttons;
    }

    public ArrayList<Button> addAllChildren(ArrayList<Button> buttons, Button button) {
        if (button.hasChildren()) {
            button.getChildren().forEach(button1 -> {
                buttons.add(button1);
                if (button1.hasChildren())
                    addAllChildren(buttons, button1);
            });
        }
        return buttons;
    }

    public ArrayList<Button> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
