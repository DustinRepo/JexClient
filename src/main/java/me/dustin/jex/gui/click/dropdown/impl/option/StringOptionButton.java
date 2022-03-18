package me.dustin.jex.gui.click.dropdown.impl.option;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.option.types.StringOption;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class StringOptionButton extends DropdownOptionButton {
    protected StringOption stringOption;
    public StringOptionButton(DropdownWindow window, StringOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
        this.stringOption = option;
    }

    @Override
    public void render(MatrixStack matrixStack) {

    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (isHovered() && int_1 == 0)
            if (!EventManager.isRegistered(this))
                EventManager.register(this);
        super.click(double_1, double_2, int_1);
    }

    protected void handleKey(EventKeyPressed event) {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DropDownGui)) {
            while (EventManager.isRegistered(this))
                EventManager.unregister(this);
            return;
        }
        int keyCode = event.getKey();
        if (Screen.isPaste(keyCode)) {
            stringOption.setValue(stringOption.getValue() + MinecraftClient.getInstance().keyboard.getClipboard());
            return;
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_ESCAPE:
                while (EventManager.isRegistered(this))
                    EventManager.unregister(this);
                break;
            case GLFW.GLFW_KEY_SPACE:
                stringOption.setValue(stringOption.getValue() + " ");
                break;
            case GLFW.GLFW_KEY_BACKSPACE:
                if (stringOption.getValue().isEmpty())
                    break;
                String str = stringOption.getValue().substring(0, stringOption.getValue().length() - 1);
                stringOption.setValue(str);
                break;
            default:
                String keyName = InputUtil.fromKeyCode(keyCode, event.getScancode()).getTranslationKey().replace("key.keyboard.", "");
                if (keyName.length() == 1) {
                    if (KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                        keyName = keyName.toUpperCase();
                        if (isInt(keyName))
                            keyName = getFromNumKey(Integer.parseInt(keyName));
                    }
                    stringOption.setValue(stringOption.getValue() + keyName);
                }
                break;
        }
        event.cancel();
    }

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
}
