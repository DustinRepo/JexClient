package me.dustin.jex.gui.click.dropdown.impl.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.KeyboardHelper;
import org.lwjgl.glfw.GLFW;

public class DropdownKeybindButton extends DropdownButton {
    private final Feature feature;
    public DropdownKeybindButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, "", x, y, width, height, null);
        this.feature = feature;
        String keyString = feature.getKey() == 0 ? "None" : KeyboardHelper.INSTANCE.getKeyName(getFeature().getKey());
        this.setName("Key: " + keyString);
    }

    @Override
    public void render(PoseStack matrixStack) {
    }

    @Override
    public void click(double double_1, double double_2, int int_1) {
        if (!EventManager.isRegistered(this) && isHovered() && int_1 == 0) {
            EventManager.register(this);
            setName("Key: ...");
        }
    }

    public void unregister() {
        EventManager.unregister(this);
    }

    public Feature getFeature() {
        return feature;
    }
}
