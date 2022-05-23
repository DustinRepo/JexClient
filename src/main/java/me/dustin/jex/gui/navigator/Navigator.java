package me.dustin.jex.gui.navigator;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.navigator.impl.NavigatorFeatureButton;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;

public class Navigator extends Screen {

    public float navigatorX, navigatorY, navigatorWidth, navigatorHeight;
    private String lastSearch;

    private ArrayList<NavigatorFeatureButton> featureButtons = new ArrayList<>();
    private TextFieldWidget searchBar;
    private Scrollbar scrollbar;
    private boolean movingScrollbar;

    public Navigator() {
        super(Text.of("Navigator"));
    }

    @Override
    protected void init() {
        navigatorWidth = width / 2.f;
        navigatorHeight = height / 1.5f;
        navigatorX = width / 2.f - (navigatorWidth / 2.f);
        navigatorY = height / 2.f - (navigatorHeight / 2.f);
        loadFeatureButtons("");
        searchBar.active = true;
        searchBar.setTextFieldFocused(true);

        this.addDrawableChild(new ButtonWidget(2, height - 22, 100, 20, Text.of("Load"), button -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).read();
        }));
        this.addDrawableChild(new ButtonWidget(2, height - 44, 100, 20, Text.of("Save"), button -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).saveButton();
        }));
        this.addDrawableChild(new ButtonWidget(2, height - 66, 100, 20, Text.of("Auto-Save: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? Formatting.GREEN + "ON" : Formatting.RED + "OFF")), button -> {
            JexClient.INSTANCE.setAutoSave(!JexClient.INSTANCE.isAutoSaveEnabled());
            button.setMessage(Text.of("Auto-Save: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? Formatting.GREEN + "ON" : Formatting.RED + "OFF")));
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        }));

        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        searchBar.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        searchBar.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            renderBackground(matrices);
        Scissor.INSTANCE.cut((int)navigatorX, (int)navigatorY, (int)navigatorWidth, (int)navigatorHeight);
        featureButtons.forEach(navigatorFeatureButton -> {
            if (navigatorFeatureButton.getY() + navigatorFeatureButton.getHeight() > navigatorY && navigatorFeatureButton.getY() < navigatorY + navigatorHeight)
                navigatorFeatureButton.render(matrices);
        });
        Scissor.INSTANCE.seal();

        searchBar.active = true;
        searchBar.setTextFieldFocused(true);
        searchBar.render(matrices, mouseX, mouseY, delta);
        if (this.scrollbar != null)
            this.scrollbar.render(matrices);

        FontHelper.INSTANCE.drawWithShadow(matrices, "Search:", searchBar.x - FontHelper.INSTANCE.getStringWidth("Search: "), navigatorY - 22, -1);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Render2DHelper.INSTANCE.isHovered(navigatorX, navigatorY, navigatorWidth, navigatorHeight)) {
            if (button == 0)
                featureButtons.forEach(navigatorFeatureButton -> navigatorFeatureButton.click(mouseX, mouseX, button));
            else if (button == 1) {
                for (NavigatorFeatureButton featureButton : featureButtons) {
                    if (featureButton.isHovered()) {
                        this.searchBar = null;
                        Wrapper.INSTANCE.getMinecraft().setScreen(new NavigatorOptionScreen(this, featureButton.getFeature()));
                        return true;
                    }
                }
            }
        }
        searchBar.mouseClicked(mouseX, mouseY, button);
        if (scrollbar != null)
            if (scrollbar.isHovered()) {
                movingScrollbar = true;
            }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        searchBar.tick();
        if (searchBar.isFocused() && lastSearch != null && !lastSearch.equalsIgnoreCase(searchBar.getText())) {
            String search = searchBar.getText().toLowerCase();
            loadFeatureButtons(search);
        }
        lastSearch = searchBar.getText();
        if (movingScrollbar) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar();
            else
                movingScrollbar = false;
        }
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (featureButtons.isEmpty())
            return super.mouseScrolled(mouseX, mouseY, amount);
        if (amount > 0) {
            NavigatorFeatureButton topButton = featureButtons.get(0);
            if (topButton == null) return false;
            if (topButton.getY() < navigatorY + 2) {
                for (int i = 0; i < 20; i++) {
                    if (topButton.getY() < navigatorY + 2) {
                        for (NavigatorFeatureButton button : featureButtons) {
                            button.setY(button.getY() + 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveUp();
                    }
                }
            }
        } else if (amount < 0) {
            NavigatorFeatureButton bottomButton = featureButtons.get(featureButtons.size() - 1);
            if (bottomButton == null) return false;
            if (bottomButton.getY() + bottomButton.getHeight() > navigatorY + navigatorHeight) {
                for (int i = 0; i < 20; i++) {
                    if (bottomButton.getY() + bottomButton.getHeight() > navigatorY + navigatorHeight) {
                        for (NavigatorFeatureButton button : featureButtons) {
                            button.setY(button.getY() - 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveDown();
                    }
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private void loadFeatureButtons(String search) {
        featureButtons.clear();
        int xCount = 0, yCount = 0;

        float buttonWidth = 100;
        float buttonHeight = 25;

        int buttonsAmountHorizontal = 0;
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            float x = navigatorX + (buttonWidth + 5) * xCount;
            if (x + buttonWidth > navigatorX + navigatorWidth) {
                break;
            }
            xCount++;
            buttonsAmountHorizontal++;
        }
        xCount = 0;

        float leftX = width / 2.f - ((buttonsAmountHorizontal / 2.f) * (buttonWidth + 5));
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (shouldShowFeature(search, feature)) {
                float x = leftX + (buttonWidth + 5) * xCount;
                if (x + buttonWidth > navigatorX + navigatorWidth) {
                    yCount++;
                    x = leftX;
                    xCount = 0;
                }
                float y = navigatorY + 2 + (buttonHeight + 10) * yCount;
                NavigatorFeatureButton navigatorFeatureButton = new NavigatorFeatureButton(feature, x, y, buttonWidth, buttonHeight, new ButtonListener() {
                    @Override
                    public void invoke() {
                        feature.toggleState();
                        if (JexClient.INSTANCE.isAutoSaveEnabled())
                            ConfigManager.INSTANCE.get(FeatureFile.class).write();
                    }
                });
                this.featureButtons.add(navigatorFeatureButton);
                xCount++;
            }
        }
        if (!featureButtons.isEmpty()) {
            float contentHeight = (featureButtons.get(featureButtons.size() - 1).getY() + (featureButtons.get(featureButtons.size() - 1).getHeight())) - featureButtons.get(0).getY();
            this.scrollbar = new Scrollbar(navigatorX + navigatorWidth + 2, navigatorY, 6, navigatorHeight, navigatorHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
        if (this.searchBar == null) {
            this.searchBar = new TextFieldWidget(Wrapper.INSTANCE.getTextRenderer(), (int) leftX + (int)FontHelper.INSTANCE.getStringWidth("Search: "), (int) navigatorY - 22, buttonsAmountHorizontal * (int) (buttonWidth + 5) - 5 - (int)FontHelper.INSTANCE.getStringWidth("Search: "), 20, Text.of(""));
            searchBar.setDrawsBackground(false);
            searchBar.setFocusUnlocked(false);
            searchBar.setTextFieldFocused(true);
            //this.addSelectableChild(searchBar);
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        searchBar = null;
        loadFeatureButtons("");
    }

    private void moveScrollbar() {
        float mouseY = MouseHelper.INSTANCE.getMouseY();
        float scrollBarHoldingArea = scrollbar.getY() + (scrollbar.getHeight() / 2.f);
        float dif = mouseY - scrollBarHoldingArea;
        if (dif > 1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() + scrollbar.getHeight() < scrollbar.getViewportY() + scrollbar.getViewportHeight()) {
                    scrollbar.moveDown();
                    for (NavigatorFeatureButton button : featureButtons) {
                        button.setY(button.getY() - 1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (NavigatorFeatureButton button : featureButtons) {
                        button.setY(button.getY() + 1);
                    }
                }
            }
        }
    }

    boolean shouldShowFeature(String search, Feature feature) {
        if (search.isEmpty())
            return true;
        else return feature.getName().toLowerCase().contains(search.toLowerCase());
    }
}
