package me.dustin.jex.gui.click.navigator.impl;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class NavigatorKeybindButton extends Button {
    private Feature feature;
    public NavigatorKeybindButton(Feature feature, float x, float y, float width, float height) {
        super(null, "", x, y, width, height, null);
        this.feature = feature;
    }

    @Override
    public void draw(MatrixStack matrixStack) {
        String keyString = feature.getKey() == 0 ? "None" : KeyboardHelper.INSTANCE.getKeyName(feature.getKey());
        if (EventAPI.getInstance().alreadyRegistered(this))
            keyString = "...";
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);
        FontHelper.INSTANCE.draw(matrixStack, "Toggle Key: " + keyString, this.getX() + 2, this.getY() + (this.getHeight() / 2.f) - 4.f, -1);
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (EventAPI.getInstance().alreadyRegistered(this))
            EventAPI.getInstance().register(this);
        else {
            if (isHovered())
                EventAPI.getInstance().register(this);
        }
        super.click(double_1, double_2, int_1);
    }

    @EventListener(events = {EventKeyPressed.class})
    public void runEvent(EventKeyPressed event) {
        if (event.getType() == EventKeyPressed.PressType.IN_GAME) {
            while (EventAPI.getInstance().alreadyRegistered(this))
                EventAPI.getInstance().unregister(this);
            return;
        }

        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE || event.getKey() == GLFW.GLFW_KEY_ENTER) {
            feature.setKey(0);
            event.cancel();
        } else {
            feature.setKey(event.getKey());
        }
        while (EventAPI.getInstance().alreadyRegistered(this))
            EventAPI.getInstance().unregister(this);
        ConfigManager.INSTANCE.get(FeatureFile.class).write();
    }
}
