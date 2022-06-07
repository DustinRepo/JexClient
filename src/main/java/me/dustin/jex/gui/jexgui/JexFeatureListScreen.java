package me.dustin.jex.gui.jexgui;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.jexgui.impl.JexCategoryButton;
import me.dustin.jex.gui.jexgui.impl.JexFeatureButton;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.ButtonListener;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class JexFeatureListScreen extends Screen {
    private final Screen parentScreen;
    private final Category category;
    public JexFeatureListScreen(Screen parentScreen, Category category) {
        super(Text.translatable("jex.gui"));
        this.parentScreen = parentScreen;
        this.category = category;
    }

    private final ArrayList<JexFeatureButton> featureButtons = new ArrayList<>();

    @Override
    protected void init() {
        populateFeatures();
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        Render2DHelper.INSTANCE.fillAndBorder(matrices, JexGuiScreen.getX(), JexGuiScreen.getY(), JexGuiScreen.getRight(), JexGuiScreen.getBottom(), category.color(), 0x80303030, 1);
        drawClientText(matrices);

        Render2DHelper.INSTANCE.fill(matrices, JexGuiScreen.getX(), JexGuiScreen.getY() + 25, JexGuiScreen.getRight(), JexGuiScreen.getY() + 26, category.color());
        FontHelper.INSTANCE.drawCenteredString(matrices, category.name(), width / 2.f, JexGuiScreen.getY() + 28, category.color());
        Render2DHelper.INSTANCE.fill(matrices, JexGuiScreen.getX(), JexGuiScreen.getY() + 38, JexGuiScreen.getRight(), JexGuiScreen.getY() + 39, category.color());
        Scissor.INSTANCE.cut(JexGuiScreen.getX(), JexGuiScreen.getY() + 40, JexGuiScreen.getGuiWidth() - 2, height - 91);
        featureButtons.forEach(categoryButton -> categoryButton.render(matrices));
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
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Render2DHelper.INSTANCE.isHovered(JexGuiScreen.getX(), JexGuiScreen.getY(), JexGuiScreen.getGuiWidth(), JexGuiScreen.getBottom() - JexGuiScreen.getY()))
            return super.mouseClicked(mouseX, mouseY, button);
        for (JexFeatureButton featureButton : featureButtons) {
            if (featureButton.isHovered()) {
                if (button == 1) {
                    Wrapper.INSTANCE.getMinecraft().setScreen(new JexPropertyListScreen(this, featureButton.getFeature()));
                    return false;
                }
                featureButton.getListener().invoke();
                return false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!featureButtons.isEmpty()) {
            if (amount > 0) {
                JexFeatureButton topButton = featureButtons.get(0);
                if (topButton == null) return false;
                if (topButton.getY() < JexGuiScreen.getY() + 40) {
                    for (int i = 0; i < 20; i++) {
                        if (topButton.getY() < JexGuiScreen.getY() + 40) {
                            for (JexFeatureButton button : featureButtons) {
                                button.setY(button.getY() + 1);
                            }
                        }
                    }
                }
            } else if (amount < 0) {
                JexFeatureButton bottomButton = featureButtons.get(featureButtons.size() - 1);
                if (bottomButton == null) return false;
                if (bottomButton.getY() + bottomButton.getHeight() > JexGuiScreen.getBottom() - 1) {
                    for (int i = 0; i < 20; i++) {
                        if (bottomButton.getY() + bottomButton.getHeight() > JexGuiScreen.getBottom() - 1) {
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

    public void populateFeatures() {
        featureButtons.clear();
        int i = 0;
        for (Feature feature : Feature.getModules(category)) {
            featureButtons.add(new JexFeatureButton(feature, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + (i * 26), JexGuiScreen.getGuiWidth() - 4, 25, new ButtonListener() {
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

    public JexFeatureButton getHovered() {
        for (JexFeatureButton featureButton : featureButtons) {
            if (featureButton.isHovered())
                return featureButton;
        }
        return null;
    }

    public void drawClientText(MatrixStack matrices) {
        matrices.push();
        matrices.scale(2, 2, 1);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.name"), (width / 2.f) / 2.f, (JexGuiScreen.getY() + 5) / 2.f, ColorHelper.INSTANCE.getClientColor());
        matrices.scale(0.5f, 0.5f, 1);
        matrices.push();
    }
}
