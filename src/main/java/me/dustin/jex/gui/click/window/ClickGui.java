package me.dustin.jex.gui.click.window;


import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Gui;
import me.dustin.jex.helper.file.files.ClientSettingsFile;
import me.dustin.jex.helper.file.files.FeatureFile;
import me.dustin.jex.helper.file.files.GuiFile;
import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.gui.click.window.impl.ModuleButton;
import me.dustin.jex.gui.click.window.impl.RadarWindow;
import me.dustin.jex.gui.click.window.impl.Window;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.gui.minecraft.blocklist.SearchSelectScreen;
import me.dustin.jex.gui.minecraft.blocklist.XraySelectScreen;
import me.dustin.jex.gui.particle.ParticleManager2D;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.option.types.ColorOption;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ClickGui extends Screen {

    private Window focused;
    public static ArrayList<Window> windows = new ArrayList<>();
    private static boolean playClickSounds = false;
    private Timer timer = new Timer();
    private TextFieldWidget searchField;
    private String lastSearch = "";
    private Button autoSaveButton = null;
    private Button launchSoundButton = null;
    private Button clickSoundButton = null;
    public Gui guiModule;
    Window configWindow;
    public RadarWindow radarWindow;
    private ButtonListener save = new ButtonListener() {
        @Override
        public void invoke() {
            FeatureFile.write();
        }
    };
    private ButtonListener load = new ButtonListener() {
        @Override
        public void invoke() {
            FeatureFile.read();
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
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        if (windows.isEmpty()) {
            guiModule = (Gui) Feature.get(Gui.class);
            int count = 0;
            float windowWidth = 120;
            float windowHeight = 15;
            for (Feature.Category category : Feature.Category.values()) {
                windows.add(new Window(category.name(), 2, 2 + ((windowHeight + 2) * count), windowWidth, windowHeight));
                count++;
            }
            windows.add(configWindow = new Window("Config", 2, 2 + ((windowHeight + 2) * count), windowWidth, windowHeight));
            count++;
            windows.add(radarWindow = new RadarWindow("Radar", 2, 2 + ((windowHeight + 2) * count), windowWidth, windowHeight));
            count++;
            GuiFile.read();
            focused = configWindow;

            int childCount = 0;
            configWindow.setOpen(true);
            configWindow.getButtons().add(new Button(configWindow, "Save", configWindow.getX() + 1, (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth - 2, windowHeight, save));
            childCount++;
            configWindow.getButtons().add(new Button(configWindow, "Load", configWindow.getX() + 1, (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth - 2, windowHeight, load));
            childCount++;
            configWindow.getButtons().add(autoSaveButton = new Button(configWindow, "Auto-Save: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? "\247aON" : "\247cOFF"), configWindow.getX() + 1, (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth - 2, windowHeight, autoSaveListener));
            childCount++;
            configWindow.getButtons().add(clickSoundButton = new Button(configWindow, "Play Click Sounds: " + (playClickSounds ? "\247aON" : "\247cOFF"), configWindow.getX() + 1, (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth - 2, windowHeight, clickSoundListener));
            childCount++;
            configWindow.getButtons().add(launchSoundButton = new Button(configWindow, "Game Launch Alert: " + (JexClient.INSTANCE.playSoundOnLaunch() ? "\247aON" : "\247cOFF"), configWindow.getX() + 1, (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth - 2, windowHeight, launchSoundListener));
            childCount++;
            configWindow.getButtons().add(new Button(configWindow, "Xray Block Select", configWindow.getX() + 1, (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth - 2, windowHeight, xrayButtonListener));
            childCount++;
            configWindow.getButtons().add(new Button(configWindow, "Search Block Select", configWindow.getX() + 1, (configWindow.getY() + configWindow.getHeight()) + (configWindow.getHeight() * childCount), windowWidth - 2, windowHeight, searchButtonListener));
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
        this.addSelectableChild(searchField = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (Render2DHelper.INSTANCE.getScaledWidth() / 2) - 150, Render2DHelper.INSTANCE.getScaledHeight() - 14, 300, 12, new LiteralText("")));
        super.init();
    }

    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        windows.forEach(window -> {if (!window.getButtons().isEmpty()) window.scroll(double_1, double_2, double_3);});
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
                if (window.getName().equalsIgnoreCase("Config") || window.getName().equalsIgnoreCase("Radar"))
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
                        height += button.getFullHeight(button) + 1;
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
        updateWindowColors();
        configWindow.setColor(ColorHelper.INSTANCE.getClientColor());
        if (timer.hasPassed(100)) {
            ParticleManager2D.INSTANCE.update();
        }

        if (((Gui) Feature.get(Gui.class)).particles) {
            for (ParticleManager2D.Particle particle : ParticleManager2D.INSTANCE.getParticles()) {
                particle.draw(matrixStack);
            }
        }

        windows.forEach(window -> {
            if (window != focused)
                window.draw(matrixStack);
        });
        focused.draw(matrixStack);
        if (getHovered() != null) {
            String description = getHovered().getFeature().getDescription();
            if (!description.endsWith("."))
                description += ".";
            float x = MouseHelper.INSTANCE.getMouseX() + FontHelper.INSTANCE.getStringWidth(description) > Render2DHelper.INSTANCE.getScaledWidth() ? Render2DHelper.INSTANCE.getScaledWidth() - FontHelper.INSTANCE.getStringWidth(description) - 2 : MouseHelper.INSTANCE.getMouseX();
            float y = MouseHelper.INSTANCE.getMouseY() + 10;

            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, x, y, x + FontHelper.INSTANCE.getStringWidth(description) + 3, y + 13, ColorHelper.INSTANCE.getClientColor(), 0xa0000000, 1);
            FontHelper.INSTANCE.drawWithShadow(matrixStack, description, x + 2, y + 2, -1);
        }
        searchField.render(matrixStack, int_1, int_2, float_1);
        if (!searchField.getText().isEmpty())
            Render2DHelper.INSTANCE.fillAndBorder(matrixStack, searchField.x - 1, searchField.y - 1, searchField.x + searchField.getWidth() + 1, searchField.y + searchField.getHeight() + 1, ColorHelper.INSTANCE.getClientColor(), 0x00ffffff, 1);
        super.render(matrixStack, int_1, int_2, float_1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Render2DHelper.INSTANCE.isHovered(0, Render2DHelper.INSTANCE.getScaledHeight() - 15, Render2DHelper.INSTANCE.getScaledWidth(), 15))
            windows.forEach(window -> {
                if (window.isHoveredAtAll())
                    focused = window;
            });
        windows.forEach(window -> {
            if (window == focused || window == configWindow)
                window.click(mouseX, mouseY, button);
        });
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        windows.forEach(window -> window.keyTyped(typedChar, keyCode));
        return super.charTyped(typedChar, keyCode);
    }

    private void updateWindowColors() {
        if (guiModule.colorScheme.equalsIgnoreCase("Customize")) {
            guiModule.getOptions().forEach(option -> {
                if (option.getName().equalsIgnoreCase("Colors"))
                    return;
                Window window = getWindow(option.getName());
                if (window != null) {
                    window.setColor(((ColorOption)option).getValue());
                }
            });
        } else {
            windows.forEach(window -> {
                window.setColor(ColorHelper.INSTANCE.getClientColor());
            });
        }
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
