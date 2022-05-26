package me.dustin.jex.gui.keybind;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.keybind.Keybind;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.file.impl.KeybindFile;
import me.dustin.jex.gui.jexgui.JexPropertyListScreen;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class JexEditKeybindScreen extends Screen {
    private final Keybind keybind;
    private final Screen parent;
    private TextFieldWidget commandField;
    private ButtonWidget setKeyButton;
    private ButtonWidget saveButton;
    private int key;
    protected JexEditKeybindScreen(Keybind keybind, Screen parent) {
        super(Text.literal(""));
        this.keybind = keybind;
        this.parent = parent;
    }

    @Override
    protected void init() {
        addSelectableChild(commandField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), width / 2 - 125, height / 2 - 30, 250, 20, Text.literal("")));
        addDrawableChild(setKeyButton = new ButtonWidget(width / 2 - 125, height / 2 + 5, 250, 20, Text.literal(keybind == null ? "Key: %sNone".formatted(Formatting.AQUA) : "Key: %s%s".formatted(Formatting.AQUA, KeyboardHelper.INSTANCE.getKeyName(keybind.key()))), button -> {
            EventManager.register(this);
            setKeyButton.setMessage(Text.literal("Press a key..."));
        }));
        addDrawableChild(saveButton = new ButtonWidget(width / 2 - 125, height - 50, 250, 20, Text.literal("Save"), button -> {
            if (keybind != null) {
                Keybind.getKeybinds().remove(keybind);
                boolean isJexCommand = commandField.getText().startsWith(CommandManagerJex.INSTANCE.getPrefix());
                Keybind.add(key, isJexCommand ? commandField.getText().substring(1) : commandField.getText(), isJexCommand);
                Wrapper.INSTANCE.getMinecraft().setScreen(parent);
                ConfigManager.INSTANCE.get(KeybindFile.class).write();
            }
        }));
        addDrawableChild(new ButtonWidget(width / 2 - 125, height - 25, 250, 20, Text.literal("Cancel"), button -> {
                Wrapper.INSTANCE.getMinecraft().setScreen(parent);
        }));
        if (keybind != null) {
            commandField.setText("%s%s".formatted(keybind.isJexCommand() ? CommandManagerJex.INSTANCE.getPrefix() : "", keybind.command()));
            key = keybind.key();
        }
        super.init();
    }

    @Override
    public void tick() {
        commandField.tick();
        saveButton.active = !commandField.getText().isEmpty() && key != 0;
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        commandField.render(matrices, mouseX, mouseY, delta);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Command/Message:", width / 2.f, height / 2.f - 45, -1);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Key:", width / 2.f, height / 2.f - 5, -1);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @EventPointer
    private final EventListener<EventKeyPressed> eventListener = new EventListener<>(event -> {
        if (!(Wrapper.INSTANCE.getMinecraft().currentScreen instanceof JexEditKeybindScreen)) {
            while (EventManager.isRegistered(this))
                EventManager.unregister(this);
            return;
        }
        int keyCode = event.getKey();
        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            this.key = keyCode;
            setKeyButton.setMessage(Text.literal("Key: %s%s".formatted(Formatting.AQUA, KeyboardHelper.INSTANCE.getKeyName(keyCode))));
        } else {
            this.key = 0;
            setKeyButton.setMessage(Text.literal("Key: %sNone".formatted(Formatting.AQUA)));
        }
        while (EventManager.isRegistered(this))
            EventManager.unregister(this);
        ConfigManager.INSTANCE.get(FeatureFile.class).write();
        event.cancel();
    });
}
