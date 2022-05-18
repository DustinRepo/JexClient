package me.dustin.jex.gui.click.dropdown.theme;

import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import net.minecraft.client.util.math.MatrixStack;
import java.util.ArrayList;

public class DropdownTheme {
    private final String name;
    private int buttonSize;
    private int buttonOffset;
    private int buttonWidthOffset;
    private int optionOffset;
    private int optionButtonOffset;
    private int optionWidthOffset;
    private int topBarSize;
    private int topBarOffset;
    private int resizeBoxSize;
    private int bottomOffset;

    public DropdownTheme(String name) {
        this.name = name;
    }

    public ArrayList<DropdownWindow> windows = new ArrayList<>();
    private DropdownWindow topWindow;

    public void init() {

    }

    public void render(MatrixStack matrixStack) {
        windows.forEach(dropdownWindow -> {
            if (dropdownWindow != topWindow)
                dropdownWindow.render(matrixStack);
        });
        if (topWindow != null)
            topWindow.render(matrixStack);
    }

    public void tick() {
        windows.forEach(dropdownWindow -> dropdownWindow.tick());
    }

    public void click(double mouseX, double mouseY, int mouseButton) {
        windows.forEach(dropdownWindow -> {
            if (dropdownWindow.isHovered() || dropdownWindow.isHoveredResize()) {
                topWindow = dropdownWindow;
            }
        });
        if (topWindow != null)
            topWindow.click(mouseX, mouseY, mouseButton);
    }

    public void scroll(double amount) {
        windows.forEach(dropdownWindow -> dropdownWindow.scroll(amount));
    }

    public DropdownWindow getWindow(String name) {
        for (DropdownWindow window : windows) {
            if (window.getName().equalsIgnoreCase(name))
                return window;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getButtonSize() {
        return buttonSize;
    }

    public void setButtonSize(int buttonSize) {
        this.buttonSize = buttonSize;
    }

    public int getButtonOffset() {
        return buttonOffset;
    }

    public void setButtonOffset(int buttonOffset) {
        this.buttonOffset = buttonOffset;
    }

    public int getOptionOffset() {
        return optionOffset;
    }

    public void setOptionOffset(int optionOffset) {
        this.optionOffset = optionOffset;
    }

    public int getTopBarSize() {
        return topBarSize;
    }

    public void setTopBarSize(int topBarSize) {
        this.topBarSize = topBarSize;
    }

    public int getTopBarOffset() {
        return topBarOffset;
    }

    public void setTopBarOffset(int topBarOffset) {
        this.topBarOffset = topBarOffset;
    }

    public int getResizeBoxSize() {
        return resizeBoxSize;
    }

    public void setResizeBoxSize(int resizeBoxSize) {
        this.resizeBoxSize = resizeBoxSize;
    }

    public int getBottomOffset() {
        return bottomOffset;
    }

    public void setBottomOffset(int bottomOffset) {
        this.bottomOffset = bottomOffset;
    }

    public int getOptionWidthOffset() {
        return optionWidthOffset;
    }

    public void setOptionWidthOffset(int optionWidthOffset) {
        this.optionWidthOffset = optionWidthOffset;
    }

    public int getButtonWidthOffset() {
        return buttonWidthOffset;
    }

    public void setButtonWidthOffset(int buttonWidthOffset) {
        this.buttonWidthOffset = buttonWidthOffset;
    }

    public int getOptionButtonOffset() {
        return optionButtonOffset;
    }

    public void setOptionButtonOffset(int optionButtonOffset) {
        this.optionButtonOffset = optionButtonOffset;
    }

    public DropdownWindow getTopWindow() {
        return topWindow;
    }
}
