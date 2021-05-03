package me.dustin.jex.gui.click.jex;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.feature.impl.render.Gui;
import me.dustin.jex.feature.impl.render.Hud;
import me.dustin.jex.file.ClientSettingsFile;
import me.dustin.jex.file.FeatureFile;
import me.dustin.jex.gui.click.jex.impl.JexOptionButton;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.option.Option;
import me.dustin.jex.option.OptionManager;
import me.dustin.jex.option.types.ColorOption;
import me.dustin.jex.option.types.StringOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class JexGui extends Screen {
    public static JexGui INSTANCE;
    private float x,y,lastwidth = 350,lastheight= 200,windowWidth = 350,windowHeight = 200;

    private ArrayList<Button> categoryButtons = new ArrayList<>();
    public ArrayList<Button> featureButtons = new ArrayList<>();
    public ArrayList<Button> optionButtons = new ArrayList<>();

    private FeatureCategory currentCategory = FeatureCategory.values()[0];
    private Button currentFeature = null;

    private Button autoSaveButton = null;
    private Button launchSoundButton = null;
    private Button clickSoundButton = null;
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
            ClickGui.setDoesPlayClickSound(!ClickGui.doesPlayClickSound());
            clickSoundButton.setName("Play Click Sounds: " + (ClickGui.doesPlayClickSound() ? "\247aON" : "\247cOFF"));
            ClientSettingsFile.write();
        }
    };
    public JexGui(Text title) {
        super(title);
        INSTANCE = this;
    }

    @Override
    protected void init() {
        if (height != lastheight || width != lastwidth) {
            resize(width, height);
        } else {
            x = (width / 2) - (windowWidth / 2);
            y = (height / 2) - (windowHeight / 2);
        }
        if (categoryButtons.isEmpty()) {
            int catCount = 0;
            for (FeatureCategory category : FeatureCategory.values()) {
                ButtonListener listener = new ButtonListener() {
                    @Override
                    public void invoke() {
                        loadFeatures(category);
                        currentCategory = category;
                    }
                };
                float topLineY = y + 15;
                Button jexGuiButton = new Button(null, category.name(), x + 1, topLineY + (catCount * 12), (windowWidth / 3) - 2, 12, listener);
                jexGuiButton.setTextColor(Hud.getCategoryColor(category));
                categoryButtons.add(jexGuiButton);

                catCount++;
            }
            int childCount = 1;
            categoryButtons.add(launchSoundButton = new Button(null,"Game Launch Alert: " + (JexClient.INSTANCE.playSoundOnLaunch() ? "\247aON" : "\247cOFF"), x + 1, y + windowHeight - 1 - (childCount * 12), (windowWidth / 3) - 1, 12, launchSoundListener));
            childCount++;
            categoryButtons.add(clickSoundButton = new Button(null,"Play Click Sounds: " + (ClickGui.doesPlayClickSound() ? "\247aON" : "\247cOFF"), x + 1, y + windowHeight - 1 - (childCount * 12), (windowWidth / 3) - 1, 12, clickSoundListener));
            childCount++;
            categoryButtons.add(autoSaveButton = new Button(null,"Auto-Save: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? "\247aON" : "\247cOFF"), x + 1, y + windowHeight - 1 - (childCount * 12), (windowWidth / 3) - 1, 12, autoSaveListener));
            childCount++;
            categoryButtons.add(new Button(null,"Load", x + 1, y + windowHeight - 1 - (childCount * 12), (windowWidth / 3) - 1, 12, load));
            childCount++;
            categoryButtons.add(new Button(null,"Save", x + 1, y + windowHeight - 1 - (childCount * 12), (windowWidth / 3) - 1, 12, save));
            childCount++;


            loadFeatures(FeatureCategory.values()[0]);
        }
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        float oneThird = (windowWidth / 3) - 2;
        float topLineY = y + 15;
        Render2DHelper.INSTANCE.fillAndBorder(matrices, x, y, x + windowWidth, y + windowHeight, ColorHelper.INSTANCE.getClientColor(), 0x60000000, 1);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Jex Client", x + ((1 + oneThird) / 2), y + 4.5f, ColorHelper.INSTANCE.getClientColor());
        FontHelper.INSTANCE.drawCenteredString(matrices, currentCategory.name(), x + (windowWidth / 2), y + 4.5f, ColorHelper.INSTANCE.getClientColor());
        FontHelper.INSTANCE.drawCenteredString(matrices, currentFeature == null ? "" : currentFeature.getName(), x + (oneThird * 2)+ ((2 + oneThird) / 2), y + 4.5f, ColorHelper.INSTANCE.getClientColor());

        Render2DHelper.INSTANCE.fill(matrices, x, topLineY, x + windowWidth, topLineY + 1, ColorHelper.INSTANCE.getClientColor());
        Render2DHelper.INSTANCE.drawVLine(matrices, x + 1 + oneThird, topLineY, y + windowHeight, ColorHelper.INSTANCE.getClientColor());
        Render2DHelper.INSTANCE.drawVLine(matrices, x + 2 + (oneThird * 2), topLineY, y + windowHeight, ColorHelper.INSTANCE.getClientColor());

        Scissor.INSTANCE.cut((int)x, (int)topLineY + 1, (int)windowWidth, (int)windowHeight - 17);
        categoryButtons.forEach(jexGuiButton -> {
            jexGuiButton.setPlayClick(ClickGui.doesPlayClickSound());
            try {
                FeatureCategory cat = FeatureCategory.valueOf(jexGuiButton.getName().toUpperCase());
                jexGuiButton.setTextColor(cat.equals(currentCategory) ? -1 : Hud.getCategoryColor(cat));
                jexGuiButton.setBackgroundColor(cat.equals(currentCategory) ? Hud.getCategoryColor(cat) : 0x00000000);
            }catch (IllegalArgumentException | NullPointerException e) {
                //button that is not a category, e.g. "Save"
                jexGuiButton.setBackgroundColor(0x00000000);
            }
            jexGuiButton.setCenterText(false);
            jexGuiButton.draw(matrices);
        });
        featureButtons.forEach(jexGuiButton -> {
            jexGuiButton.setPlayClick(ClickGui.doesPlayClickSound());
            jexGuiButton.setCenterText(false);
            jexGuiButton.setBackgroundColor(jexGuiButton.equals(currentFeature) ? Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(ColorHelper.INSTANCE.getClientColor())).darker().getRGB() : 0x00000000);
            jexGuiButton.draw(matrices);
        });
        optionButtons.forEach(jexGuiButton -> {jexGuiButton.draw(matrices); jexGuiButton.setPlayClick(ClickGui.doesPlayClickSound());});
        Scissor.INSTANCE.seal();
        Gui.clickgui.radarWindow.draw(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        lastwidth = width;
        lastheight = height;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 0) {
            if (!featureButtons.isEmpty()) {
                Button topButton = featureButtons.get(0);
                if (topButton != null && MouseHelper.INSTANCE.getMouseX() > topButton.getX() && MouseHelper.INSTANCE.getMouseX() < topButton.getX() + topButton.getWidth()) {
                    float topY = y + 16;
                    if (topButton.getY() < topY) {
                        for (int i = 0; i < 20; i++) {
                            if (topButton.getY() < topY)
                                for (Button button : featureButtons) {
                                    button.move(0, 1);
                                    moveAll(button, 0, 1);
                                }
                        }
                    }
                }
            }
            if (!optionButtons.isEmpty()) {
                Button topJexButton = optionButtons.get(0);
                if (topJexButton != null && MouseHelper.INSTANCE.getMouseX() > topJexButton.getX() && MouseHelper.INSTANCE.getMouseX() < topJexButton.getX() + topJexButton.getWidth()) {
                    float topY = y + 16;
                    if (topJexButton.getY() < topY) {
                        for (int i = 0; i < 20; i++) {
                            if (topJexButton.getY() < topY)
                                for (Button button : optionButtons) {
                                    button.move(0, 1);
                                    moveAll(button, 0, 1);
                                }
                        }
                    }
                }
            }
        } else if (amount < 0) {
            if (!featureButtons.isEmpty()) {
                Button bottomButton = featureButtons.get(featureButtons.size() - 1);
                if (bottomButton != null && MouseHelper.INSTANCE.getMouseX() > bottomButton.getX() && MouseHelper.INSTANCE.getMouseX() < bottomButton.getX() + bottomButton.getWidth()) {
                    if (bottomButton.getY() + bottomButton.getHeight() > y + windowHeight) {
                        for (int i = 0; i < 20; i++) {
                            if (bottomButton.getY() + bottomButton.getHeight() > y + windowHeight)
                                for (Button button : featureButtons) {
                                    button.move(0, -1);
                                    moveAll(button, 0, -1);
                                }
                        }
                    }
                }
            }
            if (!optionButtons.isEmpty()) {
                Button bottomJexButton = getVeryBottomButton();
                if (bottomJexButton != null && MouseHelper.INSTANCE.getMouseX() > bottomJexButton.getX() && MouseHelper.INSTANCE.getMouseX() < bottomJexButton.getX() + bottomJexButton.getWidth()) {
                    if (bottomJexButton.getY() + bottomJexButton.getHeight() > y + windowHeight) {
                        for (int i = 0; i < 20; i++) {
                            if (bottomJexButton.getY() + bottomJexButton.getHeight() > y + windowHeight)
                                for (Button button : optionButtons) {
                                    button.move(0, -1);
                                    moveAll(button, 0, -1);
                                }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        resize(width, height);
        super.resize(client, width, height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float topLineY = y + 15;
        if (!Render2DHelper.INSTANCE.isHovered(x, topLineY, windowWidth, windowHeight - 15))
            return false;
        categoryButtons.forEach(jexGuiButton -> jexGuiButton.click(mouseX, mouseY, button));
        for (Button featureButton : featureButtons) {
            if (featureButton.isHovered()) {
                Feature feature = Feature.get(featureButton.getName());
                if (feature == null)
                    return false;
                if (button == 0) {
                    feature.toggleState();
                    featureButton.setTextColor(feature.getState() ? -1 : 0xff656565);
                    if (JexClient.INSTANCE.isAutoSaveEnabled()) {
                        FeatureFile.write();
                    }
                }
                if (featureButton.isPlayClick())
                    Wrapper.INSTANCE.getMinecraft().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                loadOptions(feature);
                currentFeature = featureButton;
            }
        }
        optionButtons.forEach(jexGuiButton -> jexGuiButton.click(mouseX, mouseY, button));
        Gui.clickgui.radarWindow.click(mouseX, mouseY, button);
        return false;
    }

    private void loadFeatures(FeatureCategory featureCategory) {
        featureButtons.clear();
        int featCount = 0;
        for (Feature feature : Feature.getModules(featureCategory)) {
            float oneThird = (windowWidth / 3) - 2;
            float topLineY = y + 15;
            Button jexGuiButton = new Button(null, feature.getName(), x + 2 + oneThird, topLineY + 1 + (featCount * 15), (windowWidth / 3) - 2, 15, null);
            jexGuiButton.setTextColor(feature.getState() ? -1 : 0xff656565);
            featureButtons.add(jexGuiButton);
            featCount++;
        }
    }
    private float buttonHeight = 0;
    private void loadOptions(Feature feature) {
        buttonHeight = 0;
        optionButtons.clear();
        addExtraButtons(feature);
        for (Option option : OptionManager.get().getOptions(feature)) {
            if (!option.hasParent()) {
                float oneThird = (windowWidth / 3) - 2;
                float topLineY = y + 15;
                JexOptionButton jexGuiButton = new JexOptionButton(option, x + 3 + (oneThird * 2), topLineY + 1 + buttonHeight, (windowWidth / 3) + 1, 15);

                if (option instanceof ColorOption)
                    jexGuiButton.setHeight(100);

                if (option instanceof StringOption)
                    jexGuiButton.setHeight(15 + 10);

                optionButtons.add(jexGuiButton);
                buttonHeight += jexGuiButton.getHeight();
            }
        }
    }

    public void moveAll(Button button, float x, float y) {
        button.getChildren().forEach(button1 -> {
            button1.move(x, y);
            if (button1.hasChildren())
                moveAll(button1, x, y);
        });
    }

    private void resize(float width, float height) {
        float lastX = x;
        float lastY = y;
        x = (width / 2.0f) - (windowWidth / 2.0f);
        y = (height / 2.0f) - (windowHeight / 2.0f);

        categoryButtons.forEach(jexGuiButton -> jexGuiButton.move(x - lastX, y - lastY));
        featureButtons.forEach(jexGuiButton -> jexGuiButton.move(x - lastX, y - lastY));
        optionButtons.forEach(jexGuiButton -> {jexGuiButton.move(x - lastX, y - lastY);moveAll(jexGuiButton, x - lastX, y - lastY);});
    }

    public Button getVeryBottomButton() {
        if (optionButtons.size() == 0)
            return null;
        Button b = optionButtons.get(optionButtons.size() - 1);
        while (b.hasChildren() && b.isOpen()) {
            b = b.getChildren().get(b.getChildren().size() - 1);
        }
        return b;
    }

    public void addExtraButtons(Feature feature) {
        float oneThird = (windowWidth / 3) - 2;
        float topLineY = y + 15;
        String keyString = feature.getKey() == 0 ? "<>" : (GLFW.glfwGetKeyName(feature.getKey(), 0) == null ? InputUtil.fromKeyCode(feature.getKey(), 0).getTranslationKey().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(feature.getKey(), 0).toUpperCase()).replace("key.keyboard.", "").replace(".", "_");
        Button keyButton = new Button(null,"Key: " + (keyString.equalsIgnoreCase("0") ? "<>" : keyString.toUpperCase()), x + 3 + (oneThird * 2), topLineY + 1 + buttonHeight, (windowWidth / 3) - 1, 15, null);
        buttonHeight += keyButton.getHeight();
        Button visibleButton = new Button(null,"Visible: " + feature.isVisible(), x + 3 + (oneThird * 2), topLineY + 1 + buttonHeight, (windowWidth / 3) - 1, 15, null);
        buttonHeight += visibleButton.getHeight();
        ButtonListener keybind = new ButtonListener() {

            @EventListener(events = {EventKeyPressed.class})
            public void runEvent(EventKeyPressed event) {
                if (event.getType() == EventKeyPressed.PressType.IN_GAME) {
                    while (EventAPI.getInstance().alreadyRegistered(this))
                        EventAPI.getInstance().unregister(this);
                    return;
                }
                Button thisButton = keyButton;

                if (event.getKey() == GLFW.GLFW_KEY_ESCAPE || event.getKey() == GLFW.GLFW_KEY_ENTER) {
                    feature.setKey(0);
                    thisButton.setName("Key: <>");
                } else {
                    feature.setKey(event.getKey());
                    thisButton.setName("Key: " + (GLFW.glfwGetKeyName(event.getKey(), event.getScancode()) == null ? InputUtil.fromKeyCode(event.getKey(), event.getScancode()).getTranslationKey().replace("key.keyboard.", "").replace(".", "_") : GLFW.glfwGetKeyName(event.getKey(), event.getScancode()).toUpperCase()).toUpperCase().replace("key.keyboard.", "").replace(".", "_"));
                }
                while (EventAPI.getInstance().alreadyRegistered(this))
                    EventAPI.getInstance().unregister(this);
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
            }

            @Override
            public void invoke() {
                Button thisButton = keyButton;
                thisButton.setName("\2479Key: ...");
                EventAPI.getInstance().register(this);
            }
        };
        ButtonListener visible = new ButtonListener() {
            @Override
            public void invoke() {
                feature.setVisible(!feature.isVisible());
                visibleButton.setName("Visible: " + feature.isVisible());
                if (JexClient.INSTANCE.isAutoSaveEnabled())
                    FeatureFile.write();
            }
        };
        keyButton.setBackgroundColor(0x00000000);
        keyButton.setCenterText(false);
        visibleButton.setBackgroundColor(0x00000000);
        visibleButton.setCenterText(false);

        keyButton.setListener(keybind);
        optionButtons.add(keyButton);
        visibleButton.setListener(visible);
        optionButtons.add(visibleButton);
    }
}
