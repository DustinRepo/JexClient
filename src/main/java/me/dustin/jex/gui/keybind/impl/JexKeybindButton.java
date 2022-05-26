package me.dustin.jex.gui.keybind.impl;

import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.keybind.Keybind;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class JexKeybindButton extends Button {
    private final Keybind keybind;
    private boolean selected;
    public JexKeybindButton(Keybind keybind, float x, float y, float width, float height, ButtonListener listener) {
        super("", x, y, width, height, listener);
        this.keybind = keybind;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        super.render(matrixStack);
        if (isSelected())
            Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), ColorHelper.INSTANCE.getClientColor(), 0x00ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Key: %s%s".formatted(Formatting.AQUA, KeyboardHelper.INSTANCE.getKeyName(keybind.key())), getX() + 2, getY() + 2, 0xff696969);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, "Command: %s%s%s".formatted(Formatting.AQUA, keybind.isJexCommand() ? CommandManagerJex.INSTANCE.getPrefix() : "", keybind.command()), getX() + 2, getY() + 13, 0xff696969);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Keybind getKeybind() {
        return keybind;
    }
}
