package me.dustin.jex.gui.jexgui;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.property.PropertyManager;
import me.dustin.jex.gui.jexgui.impl.JexFeatureButton;
import me.dustin.jex.gui.jexgui.impl.JexKeybindButton;
import me.dustin.jex.gui.jexgui.impl.JexPropertyButton;
import me.dustin.jex.gui.jexgui.impl.properties.*;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Button;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;

public class JexPropertyListScreen extends Screen {
    private final Screen parentScreen;
    private final Feature feature;
    public JexPropertyListScreen(Screen parentScreen, Feature feature) {
        super(Text.translatable("jex.gui"));
        this.parentScreen = parentScreen;
        this.feature = feature;
    }

    private final ArrayList<Button> propertyButtons = new ArrayList<>();

    @Override
    protected void init() {
        populateProperties();
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        Render2DHelper.INSTANCE.fillAndBorder(matrices, JexGuiScreen.getX(), JexGuiScreen.getY(), JexGuiScreen.getRight(), JexGuiScreen.getBottom(), feature.getCategory().color(), 0x80303030, 1);
        drawClientText(matrices);

        Render2DHelper.INSTANCE.fill(matrices, JexGuiScreen.getX(), JexGuiScreen.getY() + 25, JexGuiScreen.getRight(), JexGuiScreen.getY() + 26, feature.getCategory().color());
        FontHelper.INSTANCE.drawCenteredString(matrices, feature.getName(), width / 2.f, JexGuiScreen.getY() + 28, feature.getCategory().color());
        Render2DHelper.INSTANCE.fill(matrices, JexGuiScreen.getX(), JexGuiScreen.getY() + 38, JexGuiScreen.getRight(), JexGuiScreen.getY() + 39, feature.getCategory().color());
        Scissor.INSTANCE.cut(JexGuiScreen.getX(), JexGuiScreen.getY() + 40, JexGuiScreen.getGuiWidth() - 2, height - 91);
        propertyButtons.forEach(propertyButton -> propertyButton.render(matrices));
        Scissor.INSTANCE.seal();

        Button hovered = getHovered();
        if (hovered instanceof JexPropertyButton jexPropertyButton) {
            String desc = jexPropertyButton.getGenericProperty().getDescription();
            if (desc != null) {
                Render2DHelper.INSTANCE.fillAndBorder(matrices, 0, height - 14, FontHelper.INSTANCE.getStringWidth(desc) + 6, height, feature.getCategory().color(), 0xa0000000, 1);
                FontHelper.INSTANCE.drawWithShadow(matrices, desc, 3, height - 11, feature.getCategory().color());
            }
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB) {
            Wrapper.INSTANCE.getMinecraft().setScreen(parentScreen);
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Render2DHelper.INSTANCE.isHovered(JexGuiScreen.getX(), JexGuiScreen.getY(), JexGuiScreen.getGuiWidth(), JexGuiScreen.getBottom() - JexGuiScreen.getY()))
            return super.mouseClicked(mouseX, mouseY, button);
        propertyButtons.forEach(propertyButton -> propertyButton.click(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        propertyButtons.forEach(button -> {
            if (button instanceof JexPropertyButton jexPropertyButton)
                jexPropertyButton.tick();
        });
        Button veryBottom = getVeryBottomButton();
        if (veryBottom != null)
            while (veryBottom.getY() + veryBottom.getHeight() < JexGuiScreen.getBottom() - 1) {
                if (propertyButtons.get(0).getY() == JexGuiScreen.getY() + 40)
                    break;
                propertyButtons.forEach(jexPropertyButton -> {
                    jexPropertyButton.setY(jexPropertyButton.getY() + 1);
                    moveAll(jexPropertyButton, 0, 1);
                });
            }
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!propertyButtons.isEmpty()) {
            if (amount > 0) {
                Button topButton = propertyButtons.get(0);
                if (topButton == null) return false;
                if (topButton.getY() < JexGuiScreen.getY() + 40) {
                    for (int i = 0; i < 20; i++) {
                        if (topButton.getY() < JexGuiScreen.getY() + 40) {
                            for (Button button : propertyButtons) {
                                button.setY(button.getY() + 1);
                                moveAll(button, 0, 1);
                            }
                        }
                    }
                }
            } else if (amount < 0) {
                Button bottomButton = getVeryBottomButton();
                if (bottomButton == null) return false;
                if (bottomButton.getY() + bottomButton.getHeight() > JexGuiScreen.getBottom() - 1) {
                    for (int i = 0; i < 20; i++) {
                        if (bottomButton.getY() + bottomButton.getHeight() > JexGuiScreen.getBottom() - 1) {
                            for (Button button : propertyButtons) {
                                button.setY(button.getY() - 1);
                                moveAll(button, 0, -1);
                            }
                        }
                    }
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public void moveAll(Button button, float x, float y) {
        button.getChildren().forEach(button1 -> {
            button1.move(x, y);
            if (button1.hasChildren())
                moveAll(button1, x, y);
        });
    }

    public Button getHovered() {
        for (Button propertyButton : propertyButtons) {
            if (propertyButton.isHovered())
                return propertyButton;
            Button hover = getHovered(propertyButton);
            if (hover != null)
                return hover;
        }
        return null;
    }

    public Button getHovered(Button jexPropertyButton) {
        for (Button propertyButton : jexPropertyButton.getChildren()) {
            if (propertyButton instanceof JexPropertyButton jexPropertyButton1) {
                if (propertyButton.isHovered())
                    return jexPropertyButton1;
                Button hover = getHovered(jexPropertyButton1);
                if (hover != null)
                    return hover;
            }
        }
        return null;
    }

    public void populateProperties() {
        propertyButtons.clear();
        int buttonsHeight = 0;

        this.propertyButtons.add(new JexKeybindButton(feature, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, feature.getCategory().color()));
        buttonsHeight += 26;

        for (Property<?> property : PropertyManager.INSTANCE.get(feature.getClass())) {
            if (property.getParent() != null) continue;
            JexPropertyButton jexPropertyButton = null;
            if (property.getDefaultValue() instanceof Boolean) {
                jexPropertyButton = new JexBooleanPropertyButton((Property<Boolean>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
            } else if (property.getDefaultValue() instanceof Float) {
                jexPropertyButton = new JexFloatPropertyButton((Property<Float>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
            } else if (property.getDefaultValue() instanceof Double) {
                jexPropertyButton = new JexDoublePropertyButton((Property<Double>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
            } else if (property.getDefaultValue() instanceof Integer) {
                if (property.isKeybind())
                    jexPropertyButton = new JexKeybindPropertyButton((Property<Integer>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
                else
                    jexPropertyButton = new JexIntegerPropertyButton((Property<Integer>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
            } else if (property.getDefaultValue() instanceof Long) {
                jexPropertyButton =  new JexLongPropertyButton((Property<Long>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
            } else if (property.getDefaultValue() instanceof Color) {
                jexPropertyButton = new JexColorPropertyButton((Property<Color>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 100, propertyButtons, feature.getCategory().color());
            } else if (property.getDefaultValue() instanceof Enum<?>) {
                jexPropertyButton = new JexEnumPropertyButton((Property<Enum<?>>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
            } else if (property.getDefaultValue() instanceof String) {
                jexPropertyButton = new JexStringPropertyButton((Property<String>) property, JexGuiScreen.getX() + 2, JexGuiScreen.getY() + 40 + buttonsHeight, JexGuiScreen.getGuiWidth() - 4, 25, propertyButtons, feature.getCategory().color());
            }
            if (jexPropertyButton == null)
                continue;

            this.propertyButtons.add(jexPropertyButton);
            buttonsHeight += jexPropertyButton.getHeight() + 1;
        }
    }

    public void drawClientText(MatrixStack matrices) {
        matrices.push();
        matrices.scale(2, 2, 1);
        FontHelper.INSTANCE.drawCenteredString(matrices, Text.translatable("jex.name"), (width / 2.f) / 2.f, (JexGuiScreen.getY() + 5) / 2.f, ColorHelper.INSTANCE.getClientColor());
        matrices.scale(0.5f, 0.5f, 1);
        matrices.push();
    }

    public Button getVeryBottomButton() {
        if (propertyButtons.size() == 0)
            return null;
        Button b = propertyButtons.get(propertyButtons.size() - 1);
        while (b.hasChildren() && b.isOpen()) {
            b = b.getChildren().get(b.getChildren().size() - 1);
        }
        return b;
    }
}
