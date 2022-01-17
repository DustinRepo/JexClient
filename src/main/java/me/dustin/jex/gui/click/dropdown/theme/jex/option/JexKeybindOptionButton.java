package me.dustin.jex.gui.click.dropdown.theme.jex.option;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.option.types.KeybindOption;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.option.KeybindOptionButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class JexKeybindOptionButton extends KeybindOptionButton {
    public JexKeybindOptionButton(DropdownWindow window, KeybindOption option, float x, float y, float width, float height) {
        super(window, option, x, y, width, height);
    }

    @Override
    public void render(MatrixStack matrixStack) {
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40ffffff);
        FontHelper.INSTANCE.drawWithShadow(matrixStack, getName(), getX() + 2, getY() + (getHeight() / 2 - 4), EventManager.isRegistered(this) ? getWindow().getColor() : -1);
        super.render(matrixStack);
    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getType() == EventKeyPressed.PressType.IN_GAME) {
            EventManager.unregister(this);
            return;
        }

        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE || event.getKey() == GLFW.GLFW_KEY_ENTER) {
            this.getOption().setValue(0);
            this.setName("Key: None");
            event.cancel();
        } else {
            this.getOption().setValue(event.getKey());
            this.setName("Key: " + KeyboardHelper.INSTANCE.getKeyName(event.getKey()));
        }
        EventManager.unregister(this);
        if (JexClient.INSTANCE.isAutoSaveEnabled())
            ConfigManager.INSTANCE.get(FeatureFile.class).write();
    });
}
