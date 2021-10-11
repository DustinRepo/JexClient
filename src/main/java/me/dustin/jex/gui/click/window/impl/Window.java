package me.dustin.jex.gui.click.window.impl;


import java.util.ArrayList;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.feature.mod.impl.render.Hud;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class Window {

    int childCount = 0;
    private String name;
    private float prevX, prevY;
    private float x, y, width, height;
    private boolean isOpen;
    private boolean isDragging;
    public float maxHeight = 250;
    private float xDif, yDif;
    private boolean pinned;
    private int color = ColorHelper.INSTANCE.getClientColor();
    private ArrayList<Button> buttons = new ArrayList<>();

    public Scrollbar scrollbar;

    private Identifier pin = new Identifier("jex", "gui/click/pin.png");
    private Identifier eye = new Identifier("jex", "gui/click/visible.png");

    public Window(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        if (getCategory(this) != null)
            this.color = Hud.getCategoryColor(getCategory(this));
    }

    public void draw(MatrixStack matrixStack) {
        String dispName = this.getName().substring(0, 1) + this.getName().substring(1).toLowerCase();
        maxHeight = Math.min(Render2DHelper.INSTANCE.getScaledHeight() - this.getY() - 35, Gui.INSTANCE.maxWindowHeight);
        if (scrollbar == null) {
            float contentHeight = buttons.isEmpty() || getVeryBottomButton() == null ? 0 : (getVeryBottomButton().getY() + getVeryBottomButton().getHeight()) - buttons.get(0).getY();
            float viewportHeight = maxHeight;
            float scrollBarHeight = viewportHeight * (contentHeight / viewportHeight);
            scrollbar = new Scrollbar(getX() + getWidth() - 1, getY() + getHeight(), 1, scrollBarHeight, viewportHeight, contentHeight, -1);
        }
        float contentHeight = buttons.isEmpty() ? 0 : (getVeryBottomButton().getY() + getVeryBottomButton().getHeight()) - buttons.get(0).getY();
        scrollbar.setContentHeight(contentHeight);
        scrollbar.setViewportHeight(maxHeight);
        if (isOpen()) {
            Scissor.INSTANCE.cut((int) this.getX(), (int) this.getY() + (int) this.getHeight(), (int) this.getWidth(), (int) maxHeight + 1);
            if (this.getVeryBottomButton() != null) {

                Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), Math.min(this.getVeryBottomButton().getY() + this.getVeryBottomButton().getHeight(), this.getY() + this.getHeight() + maxHeight), 0x60101010, 0x60101010, 1);
            }
            this.getButtons().forEach(button -> {
                if (button.isVisible())
                    button.draw(matrixStack);
            });
            if (scrollbar.getContentHeight() <= scrollbar.getViewportHeight()) {
                scrollbar = null;
            }
            if (this.getVeryBottomButton() != null) {
                Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY() + this.getHeight(), this.getX() + this.getWidth(), Math.min(this.getVeryBottomButton().getY() + this.getVeryBottomButton().getHeight(), this.getY() + this.getHeight() + maxHeight) + 1, color, 0x00ffffff, 1);
            }
            if (scrollbar != null)
                scrollbar.render(matrixStack);
            Scissor.INSTANCE.seal();
        }
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), color);
        Render2DHelper.INSTANCE.fill(matrixStack, this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 33, this.getY() + this.getHeight() - 1, 0x75252525);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, dispName, this.getX() + 3, this.getY() + (this.getHeight() / 2) - 4, -1);

        boolean isHoveredPin = Render2DHelper.INSTANCE.isHovered((int) (this.getX() + this.getWidth() - 15), (int) (this.getY()), 15, 15);
        boolean isHoveredEye = Render2DHelper.INSTANCE.isHovered((int) (this.getX() + this.getWidth() - 32), (int) (this.getY()), 15, 15);

        Render2DHelper.INSTANCE.bindTexture(pin);
        Render2DHelper.INSTANCE.shaderColor(0xff000000);
        DrawableHelper.drawTexture(matrixStack, (int) (this.getX() + this.getWidth() - 14), (int) (this.getY() + 1), 0, 0, 15, 15, 15, 15);
        Render2DHelper.INSTANCE.shaderColor(isHoveredPin ? -1 : isPinned() ? ColorHelper.INSTANCE.getColor(color).darker().getRGB() : ColorHelper.INSTANCE.getColor(color).darker().darker().darker().getRGB());
        DrawableHelper.drawTexture(matrixStack, (int) (this.getX() + this.getWidth() - 15), (int) (this.getY()), 0, 0, 15, 15, 15, 15);
        Render2DHelper.INSTANCE.shaderColor(-1);

        Render2DHelper.INSTANCE.bindTexture(eye);
        Render2DHelper.INSTANCE.shaderColor(0xff000000);
        DrawableHelper.drawTexture(matrixStack, (int) (this.getX() + this.getWidth() - 31), (int) (this.getY() + 1), 0, 0, 15, 15, 15, 15);
        Render2DHelper.INSTANCE.shaderColor(isHoveredEye ? -1 : isOpen() ? ColorHelper.INSTANCE.getColor(color).darker().getRGB() : ColorHelper.INSTANCE.getColor(color).darker().darker().darker().getRGB());
        DrawableHelper.drawTexture(matrixStack, (int) (this.getX() + this.getWidth() - 32), (int) (this.getY()), 0, 0, 15, 15, 15, 15);
        Render2DHelper.INSTANCE.shaderColor(-1);

        if (this.isDragging) {
            if (!MouseHelper.INSTANCE.isMouseButtonDown(0)) {
                this.isDragging = false;
                return;
            } else {
                x = xDif + MouseHelper.INSTANCE.getMouseX();
                y = yDif + MouseHelper.INSTANCE.getMouseY();
                if (getY() < 0)
                    setY(0);
                if (getX() < 0)
                    setX(0);
                if (getY() + getHeight() > Render2DHelper.INSTANCE.getScaledHeight() - 20)
                    setY(Render2DHelper.INSTANCE.getScaledHeight() - getHeight() - 20);
                if (getX() + getWidth() > Render2DHelper.INSTANCE.getScaledWidth())
                    setX(Render2DHelper.INSTANCE.getScaledWidth() - getWidth());

                getButtons().forEach(button -> {
                    button.move(x - prevX, y - prevY);
                    moveAll(button, x - prevX, y - prevY);
                });
                if (scrollbar != null) {
                    scrollbar.setX(scrollbar.getX() + (x - prevX));
                    scrollbar.setY(scrollbar.getY() + (y - prevY));
                    scrollbar.setViewportY(scrollbar.getViewportY() + (y - prevY));
                    scrollbar.setViewportHeight(maxHeight);
                }
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
        Button b = null;
        for (Button button : getButtons()) {
            if (button.isVisible()) {
                b = button;
                while (b.hasChildren() && b.isOpen()) {
                    b = button.getChildren().get(b.getChildren().size() - 1);
                }
            }
        }
        return b;
    }

    public void click(double double_1, double double_2, int int_1) {
        if (isHovered()) {
            if (int_1 == 0) {
                boolean isHoveredPin = Render2DHelper.INSTANCE.isHovered((int) (this.getX() + this.getWidth() - 15), (int) (this.getY()), 15, 15);
                boolean isHoveredEye = Render2DHelper.INSTANCE.isHovered((int) (this.getX() + this.getWidth() - 32), (int) (this.getY()), 15, 15);
                if (isHoveredEye) {
                    this.setOpen(!this.isOpen());
                    Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.5F));
                    return;
                } else if (isHoveredPin) {
                    this.setPinned(!this.isPinned());
                    Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.5F));
                    return;
                }
                this.isDragging = true;
                xDif = x - MouseHelper.INSTANCE.getMouseX();
                yDif = y - MouseHelper.INSTANCE.getMouseY();
                prevX = x;
                prevY = y;
            }
        } else
        if (this.isOpen())
            this.getButtons().forEach(button -> {
                maxHeight = Math.min(Render2DHelper.INSTANCE.getScaledHeight() - this.getY() - 35, Gui.INSTANCE.maxWindowHeight);
                if (button.isVisible() && button.getY() < this.getY() + this.getHeight() + maxHeight)
                    button.click(double_1, double_2, int_1);
            });

        if (scrollbar != null) {
            try {
                float contentHeight = buttons.isEmpty() ? 0 : (getVeryBottomButton().getY() + getVeryBottomButton().getHeight()) - buttons.get(0).getY();
                scrollbar.setContentHeight(contentHeight);
                scrollbar.setViewportHeight(maxHeight);
            }catch (Exception e){}
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        buttons.forEach(button -> button.keyTyped(typedChar, keyCode));
    }

    public Window init() {
        try {
            Feature.Category category = Feature.Category.valueOf(this.getName());
            Feature.getModules(category).forEach(module -> {
                this.getButtons().add(new ModuleButton(this, module, this.getX() + 1, (this.getY() + this.getHeight()) + ((this.getHeight() + 1) * childCount), this.getWidth() - 2, this.getHeight()));
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
                            if (topButton.getY() < this.getY() + this.getHeight()) {
                                getButtons().forEach(button -> {
                                    button.move(0, 1);
                                    moveAll(button, 0, 1);
                                });
                                if (scrollbar != null)
                                    scrollbar.moveUp();
                            }
                        }
                    }
            } else if (double_3 < 0) {
                Button bottomButton = getVeryBottomButton();
                if (bottomButton != null)
                    if (bottomButton.getY() + bottomButton.getHeight() > this.getY() + this.height + this.maxHeight) {
                        for (int i = 0; i < 20; i++) {
                            if (bottomButton.getY() + bottomButton.getHeight() > this.getY() + this.height + this.maxHeight) {
                                for (Button button : buttons) {
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
    public ModuleButton get(Feature feature) {
        ModuleButton moduleButton = null;
        for (Button button : this.getButtons())
            if (button instanceof ModuleButton) {
                if (((ModuleButton) button).getFeature() == feature)
                    moduleButton = (ModuleButton) button;
            }
        return moduleButton;
    }

    public static Feature.Category getCategory(Window window) {
        for (Feature.Category category : Feature.Category.values()) {
            if (category.toString().equalsIgnoreCase(window.getName()))
                return category;
        }
        return null;
    }

    public Button getTopVisible() {
        for (Button button : this.getButtons())
            if (button.getY() > this.getY() + this.getHeight() && button.isVisible())
                return button;
        return null;
    }

    public Button getBottomVisible() {
        Button b = null;
        for (Button button : this.getButtons())
            if (button.getY() > this.getY() + this.getHeight() && button.isVisible())
                b = button;

         return b;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isHovered() {
        return Render2DHelper.INSTANCE.isHovered(x, y, width, height);
    }

    public boolean isHoveredAtAll() {
        return Render2DHelper.INSTANCE.isHovered(x, y, width, maxHeight);
    }
}
