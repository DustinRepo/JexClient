package me.dustin.jex.gui.click.dropdown.theme.aris.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.feature.DropdownKeybindButton;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import org.lwjgl.glfw.GLFW;

public class ArisDropdownKeybindButton extends DropdownKeybindButton {
    public ArisDropdownKeybindButton(DropdownWindow window, Feature feature, float x, float y, float width, float height) {
        super(window, feature, x, y, width, height);
    }

    @Override
    public void render(PoseStack matrixStack) {
        Render2DHelper.INSTANCE.outlineAndFill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xff000000, !EventManager.isRegistered(this) ? 0xff333333 : 0xff202020);
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x45000000);
        if (!EventManager.isRegistered(this))
            Render2DHelper.INSTANCE.drawThinHLine(matrixStack, getX(), getY() + 0.5f, getX() + getWidth() - 0.5f, 0xff4a4a4a);

        FontHelper.INSTANCE.drawWithShadow(matrixStack, getName().split(":")[0], getX() + 2, getY() + (getHeight() / 2.f - 4), -1);
        String keyName =  getName().split(":")[1];
        FontHelper.INSTANCE.drawWithShadow(matrixStack, keyName, getX() + getWidth() - FontHelper.INSTANCE.getStringWidth(keyName) - 2, getY() + (getHeight() / 2.f - 4), -1);
    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getType() == EventKeyPressed.PressType.IN_GAME) {
            this.unregister();
            return;
        }

        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE || event.getKey() == GLFW.GLFW_KEY_ENTER) {
            this.getFeature().setKey(0);
            this.setName("Key: None");
            event.cancel();
        } else {
            this.getFeature().setKey(event.getKey());
            this.setName("Key: " + KeyboardHelper.INSTANCE.getKeyName(event.getKey()));
        }
        this.unregister();
        if (JexClient.INSTANCE.isAutoSaveEnabled())
            ConfigManager.INSTANCE.get(FeatureFile.class).write();
    });
}
