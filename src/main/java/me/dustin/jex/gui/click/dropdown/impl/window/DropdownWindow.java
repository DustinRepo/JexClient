package me.dustin.jex.gui.click.dropdown.impl.window;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownFeatureButton;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownKeybindButton;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownVisibleButton;
import me.dustin.jex.gui.click.dropdown.impl.option.DropdownOptionButton;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scrollbar;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;

public class DropdownWindow {

    private final DropdownTheme theme;
    private String name;
    private float x, y, width, height, maxHeight;
    private boolean dragging, resizing, open, pinned, movingScrollbar;
    private float xDif, yDif;
    private float prevX, prevY, prevWidth, prevHeight;
    private Scrollbar scrollbar;
    private int color;

    private static final float minWidth = 60;

    private final ArrayList<DropdownButton> buttons = new ArrayList<>();

    public DropdownWindow(DropdownTheme theme, String name, float x, float y, float width, float maxHeight) {
        this.theme = theme;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.maxHeight = maxHeight;
        this.height = getTheme().getTopBarSize();
        this.prevWidth = width;
        setOpen(true);
    }

    public void init() {}

    public void render(MatrixStack matrixStack) {}

    public void tick() {
        if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
            setDragging(false);
            setResizing(false);
            movingScrollbar = false;
        }
        buttons.forEach(dropdownButton -> dropdownButton.tick());
        if (movingScrollbar) {
            moveScrollbar();
        }
        if (isDragging()) {
            setResizing(false);
            x = xDif + MouseHelper.INSTANCE.getMouseX();
            y = yDif + MouseHelper.INSTANCE.getMouseY();

            scrollbar.setX(x + getWidth() - 1);
            scrollbar.setViewportY(getY() + getTheme().getButtonSize() + getTheme().getTopBarOffset());

            getButtons().forEach(button -> {
                button.move(x - prevX, y - prevY);
                moveAll(button, x - prevX, y - prevY);
            });

            prevX = x;
            prevY = y;
        } else if (isResizing()) {
            setDragging(false);
            width = xDif + MouseHelper.INSTANCE.getMouseX() - x;
            maxHeight = yDif + MouseHelper.INSTANCE.getMouseY() - y;
            if (width < minWidth)
                width = minWidth;
            if (maxHeight <= getTheme().getTopBarSize())
                maxHeight = getTheme().getTopBarSize() + 1;

            scrollbar.setX(x + getWidth() - 1);
            scrollbar.setViewportHeight(height - getTheme().getTopBarSize() - getTheme().getTopBarOffset() * 2);

            buttons.forEach(dropdownButton -> dropdownButton.resize(width - prevWidth, 0));
            prevWidth = width;
            prevHeight = maxHeight;
        }
        if (getVeryBottomButton() != null) {
            float currentHeight = getButtons().isEmpty() ? 0 : (getVeryBottomButton().getY() + getVeryBottomButton().getHeight()) - getButtons().get(0).getY();
            height = Math.min(maxHeight, currentHeight + 1 + getTheme().getTopBarSize() + getTheme().getTopBarOffset());
            scrollbar.setViewportHeight(height - getTheme().getTopBarSize() - getTheme().getResizeBoxSize() - getTheme().getTopBarOffset() + 1);
            scrollbar.setContentHeight(currentHeight);
        }
    }

    public void click(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (scrollbar.isHovered()) {
                movingScrollbar = true;
            } else if (isHoveredTop()) {
                setDragging(true);
                xDif = getX() - MouseHelper.INSTANCE.getMouseX();
                yDif = getY() - MouseHelper.INSTANCE.getMouseY();
                prevX = x;
                prevY = y;
            } else if (isHoveredResize() && isOpen()) {
                setResizing(true);
                xDif = getX() + getWidth() - MouseHelper.INSTANCE.getMouseX();
                yDif = getY() + maxHeight - MouseHelper.INSTANCE.getMouseY();
                prevWidth = width;
                prevHeight = height;
            }
        }
        if (isHoveredTop() && button == 1)
            this.setOpen(!this.isOpen());
        if (isOpen() && isHovered() && !isHoveredTop() && !isHoveredResize()) {
            buttons.forEach(dropdownButton -> dropdownButton.click(mouseX, mouseY, button));
        }
    }

    public void scroll(double amount) {
        if (isHovered() && isOpen()) {
            if (amount > 0) {
                DropdownButton topButton = this.buttons.get(0);
                if (topButton != null)
                    if (topButton.getY() < this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset()) {
                        for (int i = 0; i < 20; i++) {
                            if (topButton.getY() < this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset()) {
                                getButtons().forEach(button -> {
                                    button.move(0, 1);
                                    moveAll(button, 0, 1);
                                });
                                if (scrollbar != null)
                                    scrollbar.moveUp();
                            }
                        }
                    }
            } else if (amount < 0) {
                DropdownButton bottomButton = getVeryBottomButton();
                if (bottomButton != null)
                    if (bottomButton.getY() + bottomButton.getHeight() + ((bottomButton instanceof DropdownOptionButton || bottomButton instanceof DropdownVisibleButton) ? getTheme().getOptionOffset() : 0) > this.getY() + this.getHeight() - getTheme().getButtonOffset()) {
                        for (int i = 0; i < 20; i++) {
                            if (bottomButton.getY() + bottomButton.getHeight() + ((bottomButton instanceof DropdownOptionButton || bottomButton instanceof DropdownVisibleButton) ? getTheme().getOptionOffset() : 0) > this.getY() + this.getHeight() - getTheme().getButtonOffset()) {
                                for (DropdownButton button : buttons) {
                                    button.move(0, -1);
                                    moveAll(button, 0, -1);
                                }
                                if (scrollbar != null)
                                    scrollbar.moveDown();
                            }
                        }
                    }
            }
        }
    }

    private void moveScrollbar() {
        float mouseY = MouseHelper.INSTANCE.getMouseY();
        float scrollBarHoldingArea = scrollbar.getY() + (scrollbar.getHeight() / 2.f);
        float dif = mouseY - scrollBarHoldingArea;
        if (dif > 1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() + scrollbar.getHeight() < scrollbar.getViewportY() + scrollbar.getViewportHeight()) {
                    scrollbar.moveDown();
                    for (DropdownButton button : buttons) {
                        button.setY(button.getY() - 1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (DropdownButton button : buttons) {
                        button.setY(button.getY() + 1);
                    }
                }
            }
        }
    }

    public DropdownButton getVeryBottomButton() {
        if (getButtons().size() == 0)
            return null;
        DropdownButton b = getButtons().get(getButtons().size() - 1);
            while (b.hasChildren() && b.isOpen()) {
                b = b.getChildren().get(b.getChildren().size() - 1);
            }
        return b;
    }

    public void move(float x, float y) {
        prevX = getX();
        prevY = getY();
        scrollbar.setX(x + getWidth() - 1);
        scrollbar.setViewportY(y + getTheme().getTopBarSize() + getTheme().getTopBarOffset());

        getButtons().forEach(button -> {
            button.move(x - prevX, y - prevY);
            moveAll(button, x - prevX, y - prevY);
        });
        this.x = x;
        this.y = y;
    }

    public void resize(float width, float height) {
        this.prevWidth = this.width;
        this.prevHeight = this.height;
        scrollbar.setX(x + getWidth() - 1);
        scrollbar.setViewportHeight(height - getTheme().getTopBarSize() - getTheme().getTopBarOffset() * 2);
        buttons.forEach(dropdownButton -> dropdownButton.resize(width - prevWidth, 0));
        this.width = width;
        this.height = height;
    }

    public void moveAll(DropdownButton button, float x, float y) {
        button.getChildren().forEach(button1 -> {
            button1.move(x, y);
            if (button1.hasChildren())
                moveAll(button1, x, y);
        });
    }

    public DropdownFeatureButton get(Feature feature) {
        DropdownFeatureButton b = null;
        for (DropdownButton button : this.getButtons())
            if (button instanceof DropdownFeatureButton featureButton) {
                if (featureButton.getFeature() == feature)
                    b = featureButton;
            }
        return b;
    }

    public static Feature.Category getCategory(DropdownWindow window) {
        for (Feature.Category category : Feature.Category.values()) {
            if (category.toString().equalsIgnoreCase(window.getName()))
                return category;
        }
        return null;
    }

    public boolean isHoveredTop() {
        return Render2DHelper.INSTANCE.isHovered(getX(), getY(), getWidth(), getTheme().getTopBarSize());
    }

    public boolean isHoveredResize() {
        return Render2DHelper.INSTANCE.isHovered(getX() + getWidth() - getTheme().getResizeBoxSize(), getY() + maxHeight - getTheme().getResizeBoxSize(), getTheme().getResizeBoxSize() * 2, getTheme().getResizeBoxSize() * 2);
    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(getX(), getY(), getWidth(), getHeight());
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

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isResizing() {
        return resizing;
    }

    public void setResizing(boolean resizing) {
        this.resizing = resizing;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isMovingScrollbar() {
        return movingScrollbar;
    }

    public void setMovingScrollbar(boolean movingScrollbar) {
        this.movingScrollbar = movingScrollbar;
    }

    public ArrayList<DropdownButton> getButtons() {
        return buttons;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public DropdownTheme getTheme() {
        return theme;
    }

    public Scrollbar getScrollbar() {
        return scrollbar;
    }

    public void setScrollbar(Scrollbar scrollbar) {
        this.scrollbar = scrollbar;
    }

    public float getPrevX() {
        return prevX;
    }

    public void setPrevX(float prevX) {
        this.prevX = prevX;
    }

    public float getPrevY() {
        return prevY;
    }

    public void setPrevY(float prevY) {
        this.prevY = prevY;
    }

    public float getPrevWidth() {
        return prevWidth;
    }

    public void setPrevWidth(float prevWidth) {
        this.prevWidth = prevWidth;
    }

    public float getPrevHeight() {
        return prevHeight;
    }

    public void setPrevHeight(float prevHeight) {
        this.prevHeight = prevHeight;
    }
}
