package me.dustin.jex.gui.click.dropdown.impl.option;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.option.types.KeybindOption;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.KeyboardHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class KeybindOptionButton extends DropdownOptionButton{
    private final KeybindOption keybindOption;
    public KeybindOptionButton(DropdownWindow window, KeybindOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
        keybindOption = (KeybindOption) getOption();
        String keyString = keybindOption.getValue() == 0 ? "None" : KeyboardHelper.INSTANCE.getKeyName(keybindOption.getValue());
        this.setName(option.getName() + ": " + keyString);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        super.render(matrixStack);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        super.click(double_1, double_2, int_1);
        if (!EventManager.isRegistered(this) && isHovered() && int_1 == 0) {
            EventManager.register(this);
        }
    }
}
