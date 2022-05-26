package me.dustin.jex.gui.jexgui.impl.properties;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.JexPropertyListScreen;
import me.dustin.jex.gui.jexgui.impl.JexPropertyButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class JexStringPropertyButton extends JexPropertyButton {
    private final Property<String> stringProperty;
    private int currentTicks = 0;
    public JexStringPropertyButton(Property<String> stringProperty, float x, float y, float width, float height, ArrayList<JexPropertyButton> buttonList, int color) {
        super(stringProperty, x, y, width, height, buttonList, color);
        this.stringProperty = stringProperty;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getBackgroundColor());
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getStringProperty().getName(), this.getX() + (this.getWidth() / 2), this.getY() + 3, 0xffaaaaaa);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getStringProperty().value(), this.getX() + (this.getWidth() / 2), this.getY() + 14, getColor());
        if (EventManager.isRegistered(this)) {
            if (currentTicks % 4 >= 2) {
                float w = getX() + (getWidth() / 2.f) + FontHelper.INSTANCE.getStringWidth(this.getStringProperty().value()) / 2.f + 2;
                FontHelper.INSTANCE.drawCenteredString(matrixStack, "_", w, this.getY() + 14, -1);
            }
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), getColor(), 0x00ffffff, 1);
        }
        super.render(matrixStack);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0) {
            EventManager.register(this);
        }
        super.click(double_1, double_2, int_1);
    }

    @Override
    public void tick() {
        super.tick();
        currentTicks++;
    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventListener = new EventListener<>(event -> {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof JexPropertyListScreen)) {
            while (EventManager.isRegistered(this))
                EventManager.unregister(this);
            return;
        }
        int keyCode = event.getKey();
        String stringValue = getStringProperty().value();
        if (Screen.isPaste(keyCode)) {
            this.getStringProperty().setValue(stringValue + MinecraftClient.getInstance().keyboard.getClipboard());
            return;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_ESCAPE:
                while (EventManager.isRegistered(this))
                    EventManager.unregister(this);
                break;
            case GLFW.GLFW_KEY_SPACE:
                this.getStringProperty().setValue(stringValue + " ");
                break;
            case GLFW.GLFW_KEY_BACKSPACE:
                if (stringValue.isEmpty())
                    break;
                String str = stringValue.substring(0, stringValue.length() - 1);
                this.getStringProperty().setValue(str);
                break;
            default:
                String keyName = InputUtil.fromKeyCode(keyCode, event.getScancode()).getTranslationKey().replace("key.keyboard.", "");
                if (keyName.length() == 1) {
                    if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                        keyName = keyName.toUpperCase();
                        if (isInt(keyName))
                            keyName = getFromNumKey(Integer.parseInt(keyName));
                    }
                    this.getStringProperty().setValue(stringValue + keyName);
                }
                break;
        }
        int len = String.valueOf(this.getStringProperty().value()).length();
        if (len > this.getStringProperty().getMax())
            this.getStringProperty().setValue(String.valueOf(this.getStringProperty().value()).substring(0, (int)getStringProperty().getMax()));
        event.cancel();
    });

    private boolean isInt(String intStr) {
        try {
            Integer.parseInt(intStr);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private String getFromNumKey(int i) {
        return switch (i) {
            case 1 -> "!";
            case 2 -> "@";
            case 3 -> "#";
            case 4 -> "$";
            case 5 -> "%";
            case 6 -> "^";
            case 7 -> "&";
            case 8 -> "*";
            case 9 -> "(";
            case 0 -> ")";
            default -> String.valueOf(i);
        };
    }

    public Property<String> getStringProperty() {
        return this.stringProperty;
    }
}
