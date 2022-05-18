package me.dustin.jex.gui.click.navigator.impl;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class NavigatorKeybindButton extends Button {
    private Feature feature;
    public NavigatorKeybindButton(Feature feature, float x, float y, float width, float height) {
        super("", x, y, width, height, null);
        this.feature = feature;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        String keyString = feature.getKey() == 0 ? "None" : KeyboardHelper.INSTANCE.getKeyName(feature.getKey());
        if (EventManager.isRegistered(this))
            keyString = "...";
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        FontHelper.INSTANCE.draw(matrixStack, "Toggle Key: " + keyString, this.getX() + 2, this.getY() + (this.getHeight() / 2.f) - 4.f, -1);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (EventManager.isRegistered(this))
            EventManager.register(this);
        else {
            if (isHovered())
                EventManager.register(this);
        }
        super.click(double_1, double_2, int_1);
    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getType() == EventKeyPressed.PressType.IN_GAME) {
            while (EventManager.isRegistered(this))
                EventManager.unregister(this);
            return;
        }

        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE || event.getKey() == GLFW.GLFW_KEY_ENTER) {
            feature.setKey(0);
            event.cancel();
        } else {
            feature.setKey(event.getKey());
        }
        while (EventManager.isRegistered(this))
            EventManager.unregister(this);
        if (JexClient.INSTANCE.isAutoSaveEnabled())
            ConfigManager.INSTANCE.get(FeatureFile.class).write();
    });
}
