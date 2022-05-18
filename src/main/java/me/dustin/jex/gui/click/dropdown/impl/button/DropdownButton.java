package me.dustin.jex.gui.click.dropdown.impl.button;


import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import java.util.ArrayList;
import java.util.function.Consumer;

public class DropdownButton {

    protected Gui gui;
    private DropdownWindow window;
    private String name;
    private float x, y, width, height;
    private boolean isOpen;
    private boolean isEnabled;
    private boolean centerText = true;
    private boolean isVisible = true;
    private boolean playClick = false;
    private Consumer<Void> consumer;

    private final ArrayList<DropdownButton> children = new ArrayList<>();

    private int textColor;
    private int backgroundColor;

    public DropdownButton(DropdownWindow window, String name, float x, float y, float width, float height, Consumer<Void> consumer) {
        this.window = window;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.consumer = consumer;
        this.isEnabled = true;
        gui = Feature.get(Gui.class);
        this.backgroundColor = 0x80000000;
        this.textColor = 0xffaaaaaa;
    }

    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, x, y, x + width, y + height, backgroundColor);
        if (centerText)
            FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getName(), this.getX() + (this.getWidth() / 2), this.getY() + (this.getHeight() / 2) - 3.5f, isEnabled ? textColor : 0xff676767);
        else
            FontHelper.INSTANCE.drawWithShadow(matrixStack, this.getName(), this.getX() + 3, this.getY() + (this.getHeight() / 2) - 3.5f, isEnabled ? textColor : 0xff676767);
        if (isHovered() && isEnabled)
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        this.getChildren().forEach(button -> {
            if (button.isVisible())
                button.render(matrixStack);
        });
    }

    public void click(double double_1, double double_2, int int_1) {
        if (this.isHovered() && isEnabled) {
            if (int_1 == 0) {
                if (this.consumer != null) {
                    this.consumer.accept(null);
                }
            }
            if (int_1 == 1) {
                this.setOpen(!this.isOpen());
            }
            if (isPlayClick())
                Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        this.children.forEach(button -> {
            if (button != null) keyTyped(typedChar, keyCode);
        });
    }

    public void tick() {}

    public void resize(float width, float height) {
        this.setWidth(this.width + width);
        this.setHeight(this.height + height);

        children.forEach(dropdownButton -> dropdownButton.resize(width, height));
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

    public Consumer<Void> getListener() {
        return consumer;
    }

    public void setListener(Consumer<Void> consumer) {
        this.consumer = consumer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DropdownWindow getWindow() {
        return window;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isPlayClick() {
        return playClick;
    }

    public void setPlayClick(boolean playClick) {
        this.playClick = playClick;
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    public void move(float x, float y) {
        this.setX(this.getX() + x);
        this.setY(this.getY() + y);
    }

    public float getFullHeight(DropdownButton b) {
        float height = b.getHeight();
        if (b.isOpen())
            for (DropdownButton button : b.getChildren()) {
                height += button.getHeight();
                if (button.hasChildren() && button.isOpen())
                    height += getFullHeight(button);
            }
        return height;
    }

    public ArrayList<DropdownButton> allButtonsAfter() {
        ArrayList<DropdownButton> buttons = new ArrayList<>();
        for (DropdownButton button : getWindow().getButtons()) {
            if (getWindow().getButtons().indexOf(button) > getWindow().getButtons().indexOf(this) && button.isVisible()) {
                buttons.add(button);
                buttons = addAllChildren(buttons, button);
            }
        }
        return buttons;
    }

    public ArrayList<DropdownButton> allButtonsAfter(DropdownButton button1) {
        ArrayList<DropdownButton> buttons = new ArrayList<>();
        for (DropdownButton button : getWindow().getButtons()) {
            if (getWindow().getButtons().indexOf(button) > getWindow().getButtons().indexOf(button1) && button.isVisible()) {
                buttons.add(button);
                buttons = addAllChildren(buttons, button);
            }
        }
        return buttons;
    }

    public ArrayList<DropdownButton> addAllChildren(ArrayList<DropdownButton> buttons, DropdownButton button) {
        if (button.hasChildren()) {
            button.getChildren().forEach(button1 -> {
                buttons.add(button1);
                if (button1.hasChildren())
                    addAllChildren(buttons, button1);
            });
        }
        return buttons;
    }

    public ArrayList<DropdownButton> getChildren() {
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

    public void setCenterText(boolean centerText) {
        this.centerText = centerText;
    }
}
