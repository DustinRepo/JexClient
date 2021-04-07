package me.dustin.jex.gui.click;


import me.dustin.jex.JexClient;
import me.dustin.jex.file.ClientSettingsFile;
import me.dustin.jex.file.GuiFile;
import me.dustin.jex.file.ModuleFile;
import me.dustin.jex.gui.click.impl.Button;
import me.dustin.jex.gui.click.impl.ModuleButton;
import me.dustin.jex.gui.click.impl.Window;
import me.dustin.jex.gui.click.listener.ButtonListener;
import me.dustin.jex.gui.minecraft.blocklist.SearchSelectScreen;
import me.dustin.jex.gui.minecraft.blocklist.XraySelectScreen;
import me.dustin.jex.gui.particle.ParticleManager2D;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.render.Gui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ClickGui extends Screen {

    public static ArrayList<Window> windows = new ArrayList<>();
    private static boolean playClickSounds = false;
    private Timer timer = new Timer();
    private Button autoSaveButton = null;
    private Button launchSoundButton = null;
    private Button clickSoundButton = null;
    private TextFieldWidget searchField;
    private String lastSearch = "";
    private ButtonListener save = new ButtonListener() {
        @Override
        public void invoke() {
            ModuleFile.write();
        }
    };
    private ButtonListener load = new ButtonListener() {
        @Override
        public void invoke() {
            ModuleFile.read();
        }
    };
    private ButtonListener autoSaveListener = new ButtonListener() {
        @Override
        public void invoke() {
            JexClient.INSTANCE.setAutoSave(!JexClient.INSTANCE.isAutoSaveEnabled());
            autoSaveButton.setName("Auto-Save: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? "\247aON" : "\247cOFF"));
            ClientSettingsFile.write();
        }
    };
    private ButtonListener launchSoundListener = new ButtonListener() {
        @Override
        public void invoke() {
            JexClient.INSTANCE.setPlaySoundOnLaunch(!JexClient.INSTANCE.playSoundOnLaunch());
            launchSoundButton.setName("Game Launch Alert: " + (JexClient.INSTANCE.playSoundOnLaunch() ? "\247aON" : "\247cOFF"));
            ClientSettingsFile.write();
        }
    };
    private ButtonListener clickSoundListener = new ButtonListener() {
        @Override
        public void invoke() {
            playClickSounds = !playClickSounds;
            clickSoundButton.setName("Play Click Sounds: " + (playClickSounds ? "\247aON" : "\247cOFF"));
            ClientSettingsFile.write();
        }
    };
    private ButtonListener xrayButtonListener = new ButtonListener() {
        @Override
        public void invoke() {
            Wrapper.INSTANCE.getMinecraft().openScreen(new XraySelectScreen());
        }
    };
    private ButtonListener searchButtonListener = new ButtonListener() {
        @Override
        public void invoke() {
            Wrapper.INSTANCE.getMinecraft().openScreen(new SearchSelectScreen());
        }
    };

    public ClickGui(Text title) {
        super(title);
    }

    public static Window getWindow(String name) {
        for (Window window : windows) {
            if (window.getName().equalsIgnoreCase(name))
                return window;
        }
        return null;
    }

    @Override
    public void init() {
        if (windows.isEmpty()) {
            int count = 0;
            float windowWidth = 120;
            float windowHeight = 15;
            for (ModCategory category : ModCategory.values()) {
                windows.add(new Window(category.name(), 2, 2 + ((windowHeight + 2) * count), windowWidth, windowHeight));
                count++;
            }
            Window configWindow = new Window("Config", 2, 2 + ((windowHeight + 2) * count), windowWidth, windowHeight);
            windows.add(configWindow);
            GuiFile.read();

            int childCount = 0;
            configWindow.setOpen(true);
            configWindow.getButtons().add(new Button(configWindow, "Save", configWindow.getX(), (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth, windowHeight, save));
            childCount++;
            configWindow.getButtons().add(new Button(configWindow, "Load", configWindow.getX(), (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth, windowHeight, load));
            childCount++;
            configWindow.getButtons().add(autoSaveButton = new Button(configWindow, "Auto-Save: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? "\247aON" : "\247cOFF"), configWindow.getX(), (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth, windowHeight, autoSaveListener));
            childCount++;
            configWindow.getButtons().add(clickSoundButton = new Button(configWindow, "Play Click Sounds: " + (playClickSounds ? "\247aON" : "\247cOFF"), configWindow.getX(), (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth, windowHeight, clickSoundListener));
            childCount++;
            configWindow.getButtons().add(launchSoundButton = new Button(configWindow, "Game Launch Alert: " + (JexClient.INSTANCE.playSoundOnLaunch() ? "\247aON" : "\247cOFF"), configWindow.getX(), (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth, windowHeight, launchSoundListener));
            childCount++;
            configWindow.getButtons().add(new Button(configWindow, "Xray Block Select", configWindow.getX(), (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth, windowHeight, xrayButtonListener));
            childCount++;
            configWindow.getButtons().add(new Button(configWindow, "Search Block Select", configWindow.getX(), (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth, windowHeight, searchButtonListener));
            childCount++;

            windows.forEach(window -> {
                if (window != configWindow)
                    window.init();
            });
        }
        ParticleManager2D.INSTANCE.getParticles().clear();
        for (int i = 0; i < 50; i++) {
            ParticleManager2D.INSTANCE.add(ClientMathHelper.INSTANCE.getRandom(Render2DHelper.INSTANCE.getScaledWidth()), ClientMathHelper.INSTANCE.getRandom(Render2DHelper.INSTANCE.getScaledHeight()));
        }
        this.children.add(searchField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 150, Render2DHelper.INSTANCE.getScaledHeight() - 14, 300, 12, new LiteralText("")));
        super.init();
    }

    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        windows.forEach(window -> window.scroll(double_1, double_2, double_3));
        return false;
    }

    @Override
    public void onClose() {
        GuiFile.write();
        super.onClose();
    }

    @Override
    public void tick() {
        searchField.tick();
        if (searchField.isFocused() && lastSearch != searchField.getText()) {
            String search = searchField.getText().toLowerCase();
            for (Window window : windows) {
                if (window.getName().equalsIgnoreCase("Config"))
                    continue;
                if (search.isEmpty()) {
                    for (Button button : window.getButtons()) {
                        button.setVisible(true);
                    }
                } else {
                    for (Button button : window.getButtons()) {
                        button.setVisible(button.getName().toLowerCase().contains(search));
                    }
                }
                int height = 0;
                for (Button button : window.getButtons()) {
                    if (button instanceof ModuleButton) {
                        ModuleButton moduleButton = (ModuleButton)button;
                        moduleButton.close();
                    }
                    button.setOpen(false);
                    if (button.isVisible()) {
                        float y = button.getY();
                        button.move(0, (window.getY() + window.getHeight() + height) - y);
                        window.moveAll(button, 0, (window.getY() + window.getHeight() + height) - y);
                        height += button.getFullHeight(button);
                    }
                }
            }
        }
        lastSearch = searchField.getText();
        super.tick();
    }

    @Override
    public void render(MatrixStack matrixStack, int int_1, int int_2, float float_1) {
        renderBackground(matrixStack);
        if (timer.hasPassed(100)) {
            ParticleManager2D.INSTANCE.update();
        }

        if (((Gui) Module.get(Gui.class)).particles) {
            for (ParticleManager2D.Particle particle : ParticleManager2D.INSTANCE.getParticles()) {
                particle.draw(matrixStack);
            }
        }

        windows.forEach(window -> {
            window.draw(matrixStack);
        });

        if (getHovered() != null) {
            String description = getHovered().getModule().getDescription();
            if (!description.endsWith("."))
                description += ".";
            float x = MouseHelper.INSTANCE.getMouseX() + FontHelper.INSTANCE.getStringWidth(description) > Render2DHelper.INSTANCE.getScaledWidth() ? Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(description) - 2 : MouseHelper.INSTANCE.getMouseX();
            float y = MouseHelper.INSTANCE.getMouseY() + 10;

            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x, y, x + FontHelper.INSTANCE.getStringWidth(description) + 3, y + 13, ColorHelper.INSTANCE.getClientColor(), 0xa0000000, 1);
            FontHelper.INSTANCE.drawWithShadow(matrixStack, description, x + 2, y + 2, -1);
        }
        Render2DHelper.INSTANCE.fill(matrixStack, 0, Render2DHelper.INSTANCE.getScaledHeight() - 15, Render2DHelper.INSTANCE.getScaledWidth(), Render2DHelper.INSTANCE.getScaledHeight(), !searchField.getText().isEmpty() ? ColorHelper.INSTANCE.getClientColor() & 0x30ffffff : 0x30000000);
        searchField.render(matrixStack, int_1, int_2, float_1);
        super.render(matrixStack, int_1, int_2, float_1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Render2DHelper.INSTANCE.isHovered(0, Render2DHelper.INSTANCE.getScaledHeight() - 15, Render2DHelper.INSTANCE.getScaledWidth(), 15))
            windows.forEach(window -> window.click(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        windows.forEach(window -> window.keyTyped(typedChar, keyCode));
        return super.charTyped(typedChar, keyCode);
    }

    private ModuleButton getHovered() {
        for (Window window : windows) {
            if (window.isOpen())
                for (Button button : window.getButtons()) {
                    if (button instanceof ModuleButton && button.isHovered() && button.isVisible())
                        return (ModuleButton) button;
                }
        }
        return null;
    }

    public static boolean doesPlayClickSound() {
        return playClickSounds;
    }
    public static void setDoesPlayClickSound(boolean playClickSounds1) {
        playClickSounds = playClickSounds1;
    }
}
