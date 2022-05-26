package me.dustin.jex.gui.jexgui.impl.properties;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.JexPropertyListScreen;
import me.dustin.jex.gui.jexgui.impl.JexPropertyButton;
import me.dustin.jex.gui.navigator.NavigatorOptionScreen;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class JexKeybindPropertyButton extends JexPropertyButton {
    private final Property<Integer> keybindProperty;
    public JexKeybindPropertyButton(Property<Integer> keybindProperty, float x, float y, float width, float height, ArrayList<JexPropertyButton> buttonList, int color) {
        super(keybindProperty, x, y, width, height, buttonList, color);
        this.keybindProperty = keybindProperty;
    }

    @Override
    public void render(MatrixStack matrixStack) {
        Render2DHelper.INSTANCE.fill(matrixStack, getX(), getY(), getX() + getWidth(), getY() + getHeight(), getBackgroundColor());
        if (isHovered())
            Render2DHelper.INSTANCE.fill(matrixStack, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0x25ffffff);

        int key = getKeybindProperty().value();
        String s = EventManager.isRegistered(this) ? "Press a key..." : (key == 0 ? "None" : KeyboardHelper.INSTANCE.getKeyName(key));
        FontHelper.INSTANCE.drawCenteredString(matrixStack, this.getKeybindProperty().getName(), getX() + getWidth() / 2.f, this.getY() + 4, 0xffaaaaaa);
        FontHelper.INSTANCE.drawCenteredString(matrixStack, s, getX() + getWidth() / 2.f, this.getY() + 15, EventManager.isRegistered(this) ? 0xffaaaaaa : getColor());
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
    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventListener = new EventListener<>(event -> {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof JexPropertyListScreen)) {
            while (EventManager.isRegistered(this))
                EventManager.unregister(this);
            return;
        }
        int keyCode = event.getKey();
        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            this.getKeybindProperty().setValue(keyCode);
        } else {
            this.getKeybindProperty().setValue(0);
        }
        while (EventManager.isRegistered(this))
            EventManager.unregister(this);
        ConfigManager.INSTANCE.get(FeatureFile.class).write();
        event.cancel();
    });

    public Property<Integer> getKeybindProperty() {
        return this.keybindProperty;
    }
}
