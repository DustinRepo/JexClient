package me.dustin.jex.gui.jexgui;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.impl.JexCategoryButton;
import me.dustin.jex.gui.jexgui.impl.JexFeatureButton;
import me.dustin.jex.gui.keybind.JexKeybindListScreen;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class JexGuiScreen extends Screen {
    private final Screen parentScreen;
    private TextFieldWidget searchBar;
    private String lastSearch = "";
    private final boolean noCategories;
    public JexGuiScreen(Screen parentScreen, boolean noCategories) {
        super(Text.translatable("jex.gui"));
        this.parentScreen = parentScreen;
        this.noCategories = noCategories;
    }

    private final ArrayList<JexCategoryButton> categoryButtons = new ArrayList<>();
    private final ArrayList<JexFeatureButton> featureButtons = new ArrayList<>();

    @Override
    protected void init() {
        String searchStr = Text.translatable("jex.gui.search").getString() + " ";
        this.searchBar = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (int) (getX() + 2 + FontHelper.INSTANCE.getStringWidth(searchStr)), getY() + 28, (int) (getGuiWidth() - 4 - FontHelper.INSTANCE.getStringWidth(searchStr)), 25, Text.literal(""));
        searchBar.active = true;
        searchBar.setTextFieldFocused(true);
        populateCategories();

        this.addDrawableChild(new ButtonWidget(2, height - 22, 100, 20, Text.translatable("jex.button.load"), button -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).read();
        }));
        this.addDrawableChild(new ButtonWidget(2, height - 44, 100, 20, Text.translatable("jex.button.save"), button -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).saveButton();
        }));
        this.addDrawableChild(new ButtonWidget(2, height - 66, 100, 20, Text.translatable(JexClient.INSTANCE.isAutoSaveEnabled() ? "jex.gui.autosave.on" : "jex.gui.autosave.off"), button -> {
            JexClient.INSTANCE.setAutoSave(!JexClient.INSTANCE.isAutoSaveEnabled());
            button.setMessage(Text.translatable(JexClient.INSTANCE.isAutoSaveEnabled() ? "jex.gui.autosave.on" : "jex.gui.autosave.off"));
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        }));
        this.addDrawableChild(new ButtonWidget(2, height - 88, 100, 20, Text.translatable("jex.keybinds"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexKeybindListScreen(Wrapper.INSTANCE.getLocalPlayer() == null ? this : null));
        }));
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        Render2DHelper.INSTANCE.fillAndBorder(matrices, getX(), getY(), getRight(), getBottom(), ColorHelper.INSTANCE.getClientColor(), 0x80303030, 1);
        drawClientText(matrices);

        Render2DHelper.INSTANCE.fill(matrices, getX(), getY() + 25, getRight(), getY() + 26, ColorHelper.INSTANCE.getClientColor());
        FontHelper.INSTANCE.drawWithShadow(matrices, Text.translatable("jex.gui.search"), getX() + 2, getY() + 28, ColorHelper.INSTANCE.getClientColor());
        Render2DHelper.INSTANCE.fill(matrices, getX(), getY() + 38, getRight(), getY() + 39, ColorHelper.INSTANCE.getClientColor());
        searchBar.active = true;
        searchBar.setTextFieldFocused(true);
        searchBar.setDrawsBackground(false);
        searchBar.setFocusUnlocked(false);
        searchBar.render(matrices, mouseX, mouseY, delta);
        Scissor.INSTANCE.cut(getX(), getY() + 40, JexGuiScreen.getGuiWidth() - 2, height - 91);
        categoryButtons.forEach(categoryButton -> categoryButton.render(matrices));
        featureButtons.forEach(featureButton -> featureButton.render(matrices));
        Scissor.INSTANCE.seal();

        JexFeatureButton hovered = getHovered();
        if (hovered != null) {
            String desc = hovered.getFeature().getDescription();
            Render2DHelper.INSTANCE.fillAndBorder(matrices, 0, height - 14, FontHelper.INSTANCE.getStringWidth(desc) + 6, height, hovered.getFeature().getCategory().color(), 0xa0000000, 1);
            FontHelper.INSTANCE.drawWithShadow(matrices, desc, 3, height - 11, hovered.getFeature().getCategory().color());
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Wrapper.INSTANCE.getMinecraft().setScreen(parentScreen);
            return false;
        }
        searchBar.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        searchBar.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Render2DHelper.INSTANCE.isHovered(getX(), getY(), getGuiWidth(), getBottom() - getY()))
            return super.mouseClicked(mouseX, mouseY, button);
        for (JexCategoryButton categoryButton : categoryButtons) {
            if (categoryButton.isHovered()) {
                categoryButton.getListener().invoke();
                return false;
            }
        }
        for (JexFeatureButton featureButton : featureButtons) {
            if (featureButton.isHovered()) {
                if (button == 1) {
                    Wrapper.INSTANCE.getMinecraft().setScreen(new JexPropertyListScreen(this, featureButton.getFeature()));
                    return true;
                }
                featureButton.getListener().invoke();
                return false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        searchBar.tick();
        if (searchBar.isFocused() && lastSearch != null && !lastSearch.equalsIgnoreCase(searchBar.getText())) {
            String search = searchBar.getText().toLowerCase();
            if (search.isEmpty())
                populateCategories();
            else
                loadFeatureButtons(search);
        }
        lastSearch = searchBar.getText();
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!categoryButtons.isEmpty()) {
            if (amount > 0) {
                JexCategoryButton topButton = categoryButtons.get(0);
                if (topButton == null) return false;
                if (topButton.getY() < getY() + 40) {
                    for (int i = 0; i < 20; i++) {
                        if (topButton.getY() < getY() + 40) {
                            for (JexCategoryButton button : categoryButtons) {
                                button.setY(button.getY() + 1);
                            }
                        }
                    }
                }
            } else if (amount < 0) {
                JexCategoryButton bottomButton = categoryButtons.get(categoryButtons.size() - 1);
                if (bottomButton == null) return false;
                if (bottomButton.getY() + bottomButton.getHeight() > getBottom() - 1) {
                    for (int i = 0; i < 20; i++) {
                        if (bottomButton.getY() + bottomButton.getHeight() > getBottom() - 1) {
                            for (JexCategoryButton button : categoryButtons) {
                                button.setY(button.getY() - 1);
                            }
                        }
                    }
                }
            }
        }
        if (!featureButtons.isEmpty()) {
            if (amount > 0) {
                JexFeatureButton topButton = featureButtons.get(0);
                if (topButton == null) return false;
                if (topButton.getY() < getY() + 40) {
                    for (int i = 0; i < 20; i++) {
                        if (topButton.getY() < getY() + 40) {
                            for (JexFeatureButton button : featureButtons) {
                                button.setY(button.getY() + 1);
                            }
                        }
                    }
                }
            } else if (amount < 0) {
                JexFeatureButton bottomButton = featureButtons.get(featureButtons.size() - 1);
                if (bottomButton == null) return false;
                if (bottomButton.getY() + bottomButton.getHeight() > getBottom() - 1) {
                    for (int i = 0; i < 20; i++) {
                        if (bottomButton.getY() + bottomButton.getHeight() > getBottom() - 1) {
                            for (JexFeatureButton button : featureButtons) {
                                button.setY(button.getY() - 1);
                            }
                        }
                    }
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public JexFeatureButton getHovered() {
        for (JexFeatureButton featureButton : featureButtons) {
            if (featureButton.isHovered())
                return featureButton;
        }
        return null;
    }

    public void populateCategories() {
        categoryButtons.clear();
        featureButtons.clear();
        if (noCategories) {
            loadFeatureButtons("");
            return;
        }
        int i = 0;
        for (Category category : Category.values()) {
            categoryButtons.add(new JexCategoryButton(category, getX() + 2, getY() + 40 + (i * 26), getGuiWidth() - 4, 25, new ButtonListener() {
                @Override
                public void invoke() {
                    Wrapper.INSTANCE.getMinecraft().setScreen(new JexFeatureListScreen(JexGuiScreen.this, category));
                }
            }));
            i++;
        }
    }

    public void loadFeatureButtons(String search) {
        featureButtons.clear();
        categoryButtons.clear();
        int i = 0;
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (search.isEmpty() || shouldShowFeature(search, feature)) {
                featureButtons.add(new JexFeatureButton(feature, getX() + 2, getY() + 40 + (i * 26), getGuiWidth() - 4, 25, new ButtonListener() {
                    @Override
                    public void invoke() {
                        feature.toggleState();
                        if (JexClient.INSTANCE.isAutoSaveEnabled())
                            ConfigManager.INSTANCE.get(FeatureFile.class).write();
                    }
                }));
                i++;
            }
        }
    }

    public void drawClientText(MatrixStack matrices) {
        matrices.push();
        matrices.scale(2, 2, 1);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.name"), (width / 2.f) / 2.f, (getY() + 5) / 2.f, ColorHelper.INSTANCE.getClientColor());
        matrices.scale(0.5f, 0.5f, 1);
        matrices.push();
    }

    public static int getX() {
        return Render2DHelper.INSTANCE.getScaledWidth() / 2 - 100;
    }

    public static int getY() {
        return 25;
    }

    public static int getRight() {
        return Render2DHelper.INSTANCE.getScaledWidth() / 2 + 100;
    }

    public static int getBottom() {
        return Render2DHelper.INSTANCE.getScaledHeight() - 25;
    }

    public static int getGuiWidth() {
        return getRight() - getX();
    }

    boolean shouldShowFeature(String search, Feature feature) {
        if (search.isEmpty())
            return true;
        else return feature.getName().toLowerCase().contains(search.toLowerCase());
    }
}
