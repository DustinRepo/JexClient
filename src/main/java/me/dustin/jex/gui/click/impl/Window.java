package me.dustin.jex.gui.click.impl;


import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.render.Gui;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;

public class Window {

    private static Gui gui;
    int childCount = 0;
    private String name;
    private float prevX, prevY;
    private float x, y, width, height;
    private boolean isOpen;
    private boolean isDragging;
    private float maxHeight = 300;
    private float xDif, yDif;
    private boolean pinned;
    private ArrayList<Button> buttons = new ArrayList<>();


    public Window(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        gui = (Gui) Module.get(Gui.class);
    }

    public void draw(MatrixStack matrixStack) {
        String dispName = this.getName().substring(0, 1) + this.getName().substring(1).toLowerCase();
        maxHeight = Render2DHelper.INSTANCE.getScaledHeight() - this.getY() - 30 > 0 ? Render2DHelper.INSTANCE.getScaledHeight() - this.getY() - 30 : 250;

        if (isOpen()) {
            Scissor.INSTANCE.cut((int) x, (int) y + (int) height, (int) width, (int) maxHeight);
            this.getButtons().forEach(button -> {
                if (button.isVisible())
                    button.draw(matrixStack);
            });
            Scissor.INSTANCE.seal();
        }

        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), ColorHelper.INSTANCE.getClientColor());
        FontHelper.INSTANCE.drawCenteredString(matrixStack, dispName, this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2) - 5, -1);

        if (this.isDragging) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
                this.isDragging = false;
                return;
            } else {
                x = xDif + MouseHelper.INSTANCE.getMouseX();
                y = yDif + MouseHelper.INSTANCE.getMouseY();
                getButtons().forEach(button -> {
                    button.move(x - prevX, y - prevY);
                    moveAll(button, x - prevX, y - prevY);
                });
                prevX = x;
                prevY = y;
            }
        }
    }

    public void moveAll(Button button, float x, float y) {
        button.getChildren().forEach(button1 -> {
            button1.move(x, y);
            if (button1.hasChildren())
                moveAll(button1, x, y);
        });
    }

    public Button getVeryBottomButton() {
        if (getButtons().size() == 0)
            return null;
        Button button = getButtons().get(getButtons().size() - 1);
        while (button.hasChildren() && button.isOpen()) {
            button = button.getChildren().get(button.getChildren().size() - 1);
        }
        return button;
    }

    public void click(double double_1, double double_2, int int_1) {
        if (isHovered()) {
            if (int_1 == 0) {
                this.isDragging = true;
                xDif = x - MouseHelper.INSTANCE.getMouseX();
                yDif = y - MouseHelper.INSTANCE.getMouseY();
                prevX = x;
                prevY = y;
            }
            if (int_1 == 1) {
                this.setOpen(!this.isOpen);
                return;
            }
        }
        if (this.isOpen())
            this.getButtons().forEach(button -> {
                if (button.isVisible())
                    button.click(double_1, double_2, int_1);
            });
    }

    public void keyTyped(char typedChar, int keyCode) {
        buttons.forEach(button -> button.keyTyped(typedChar, keyCode));
    }

    public Window init() {
        try {
            ModCategory category = ModCategory.valueOf(this.getName());
            Module.getModules(category).forEach(module -> {
                this.getButtons().add(new ModuleButton(this, module, this.getX(), (this.getY() + this.getHeight()) + (this.getHeight() * childCount), this.getWidth(), this.getHeight()));
                childCount++;
            });
        } catch (Exception e) {

        }

        return this;
    }

    public void scroll(double double_1, double double_2, double double_3) {
        if (Render2DHelper.INSTANCE.isHovered(x, y, width, maxHeight + height)) {
            if (double_3 > 0) {
                Button topButton = this.buttons.get(0);
                if (topButton != null)
                    if (topButton.getY() < this.getY() + this.getHeight()) {
                        for (int i = 0; i < 20; i++) {
                            if (topButton.getY() < this.getY() + this.getHeight())
                                getButtons().forEach(button -> {
                                    button.move(0, 1);
                                    moveAll(button, 0, 1);
                                });
                        }
                    }
            } else if (double_3 < 0) {
                Button bottomButton = getVeryBottomButton();
                if (bottomButton != null)
                    if (bottomButton.getY() + bottomButton.getHeight() > this.getY() + this.height + this.maxHeight) {
                        for (int i = 0; i < 20; i++) {
                            if (bottomButton.getY() + bottomButton.getHeight() > this.getY() + this.height + this.maxHeight)
                                for (Button button : buttons) {
                                    button.move(0, -1);
                                    moveAll(button, 0, -1);
                                }
                        }
                    }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public ArrayList<Button> getButtons() {
        return buttons;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
    public ModuleButton get(Module module) {
        ModuleButton moduleButton = null;
        for (Button button : this.getButtons())
            if (button instanceof ModuleButton) {
                if (((ModuleButton) button).getModule() == module)
                    moduleButton = (ModuleButton) button;
            }
        return moduleButton;
    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(x, y, width, height);
    }
}
