package me.dustin.jex.gui.click.navigator;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.OptionManager;
import me.dustin.jex.feature.option.types.ColorOption;
import me.dustin.jex.feature.option.types.StringOption;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.gui.click.navigator.impl.NavigatorFeatureVisibleButton;
import me.dustin.jex.gui.click.navigator.impl.NavigatorKeybindButton;
import me.dustin.jex.gui.click.navigator.impl.NavigatorOptionButton;
import me.dustin.jex.gui.click.window.impl.Button;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class NavigatorOptionScreen extends Screen {

    private Navigator navigator;
    private Feature feature;
    private static boolean fade = true;
    private float lastFadeAmount, fadeAmount;

    private Scrollbar scrollbar;
    private boolean movingScrollbar;

    public static ArrayList<Button> options = new ArrayList<>();

    protected NavigatorOptionScreen(Navigator navigator, Feature feature) {
        super(new LiteralText("Navigator"));
        this.navigator = navigator;
        this.feature = feature;
        fade = true;
        options.clear();
    }

    @Override
    protected void init() {
        options.clear();
        options.add(new NavigatorKeybindButton(feature, navigator.navigatorX, navigator.navigatorY + 62, navigator.navigatorWidth, 15));
        options.add(new NavigatorFeatureVisibleButton(feature, navigator.navigatorX, navigator.navigatorY + 77, navigator.navigatorWidth, 15));
        float buttonHeight = 0;
        for (Option option : OptionManager.get().getOptions(feature)) {
            if (!option.hasParent()) {
                NavigatorOptionButton navigatorOptionButton = new NavigatorOptionButton(option, navigator.navigatorX, navigator.navigatorY + 92 + buttonHeight, navigator.navigatorWidth, 15);

                if (option instanceof ColorOption)
                    navigatorOptionButton.setHeight(100);

                if (option instanceof StringOption)
                    navigatorOptionButton.setHeight(15 + 10);
                options.add(navigatorOptionButton);
                buttonHeight += navigatorOptionButton.getHeight();
            }
        }
        float contentHeight = getVeryBottomButton().getY() + getVeryBottomButton().getHeight() - options.get(0).getY() - 1;
        float viewportHeight = navigator.navigatorHeight - 90;
        float scrollBarHeight = viewportHeight * (contentHeight / viewportHeight);
        scrollbar = new Scrollbar(navigator.navigatorX + navigator.navigatorWidth + 2, navigator.navigatorY + 62, 6, scrollBarHeight, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (Wrapper.INSTANCE.getLocalPlayer() == null)
            renderBackground(matrices);
        float sizeModifier = lastFadeAmount + ((fadeAmount - lastFadeAmount) * Wrapper.INSTANCE.getMinecraft().getTickDelta());
        Render2DHelper.INSTANCE.outlineAndFill(matrices, width / 2.f - ((navigator.navigatorWidth / 2.f) * sizeModifier), height / 2.f - ((navigator.navigatorHeight / 2.f) * sizeModifier), width / 2.f + ((navigator.navigatorWidth / 2.f) * sizeModifier), height / 2.f + ((navigator.navigatorHeight / 2.f) * sizeModifier), 0x90656565, 0x70000000);

        if (sizeModifier == 1) {//finished the fade in effect
            FontHelper.INSTANCE.drawCenteredString(matrices, feature.getName(), navigator.navigatorX + (navigator.navigatorWidth / 2.f), navigator.navigatorY - 12, -1);

            FontHelper.INSTANCE.drawWithShadow(matrices, "Category: " + StringUtils.capitalize(feature.getFeatureCategory().name().toLowerCase()), navigator.navigatorX + 2, navigator.navigatorY + 2, -1);
            FontHelper.INSTANCE.drawWithShadow(matrices, "Description:", navigator.navigatorX + 2, navigator.navigatorY + 18, -1);
            FontHelper.INSTANCE.drawWithShadow(matrices, feature.getDescription(), navigator.navigatorX + 2, navigator.navigatorY + 30, -1);
            FontHelper.INSTANCE.drawWithShadow(matrices, "Settings:", navigator.navigatorX + 2, navigator.navigatorY + 50, -1);

            float contentHeight = getVeryBottomButton().getY() + getVeryBottomButton().getHeight() - options.get(0).getY() - 1;
            scrollbar.setContentHeight(contentHeight);
            scrollbar.render(matrices);

            Scissor.INSTANCE.cut((int)navigator.navigatorX, (int)navigator.navigatorY + 62, (int)navigator.navigatorWidth,(int)navigator.navigatorHeight - 90);
            options.forEach(button -> button.draw(matrices));
            Scissor.INSTANCE.seal();

            //draw fake button
            Render2DHelper.INSTANCE.outlineAndFill(matrices, navigator.navigatorX + 4, navigator.navigatorY + navigator.navigatorHeight - 24, navigator.navigatorX + navigator.navigatorWidth - 4, navigator.navigatorY + navigator.navigatorHeight - 4, 0xff000000, feature.getState() ? ColorHelper.INSTANCE.getClientColor() & 0x50ffffff : 0x50000000);
            if (isHoveredToggleButton())
                Render2DHelper.INSTANCE.outlineAndFill(matrices, navigator.navigatorX + 4, navigator.navigatorY + navigator.navigatorHeight - 24, navigator.navigatorX + navigator.navigatorWidth - 4, navigator.navigatorY + navigator.navigatorHeight - 4, 0xff000000, 0x20ffffff);
            FontHelper.INSTANCE.drawCenteredString(matrices, feature.getState() ? "Disable" : "Enable", navigator.navigatorX + (navigator.navigatorWidth / 2.f), navigator.navigatorY + navigator.navigatorHeight - 18.5f, -1);
        }

        lastFadeAmount = fadeAmount;
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!fade) {
            if (isHoveredToggleButton()) {
                feature.toggleState();
                ConfigManager.INSTANCE.get(FeatureFile.class).write();
            }
            if (Render2DHelper.INSTANCE.isHovered(navigator.navigatorX, navigator.navigatorY, navigator.navigatorWidth, navigator.navigatorHeight - 26)) {
                options.forEach(button1 -> button1.click(mouseX, mouseX, button));
            }
            if (scrollbar != null)
                if (scrollbar.isHovered()) {
                    movingScrollbar = true;
                }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Wrapper.INSTANCE.getMinecraft().openScreen(navigator);
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (options.isEmpty())
            return super.mouseScrolled(mouseX, mouseY, amount);
        if (amount > 0) {
            Button topButton = options.get(0);
            if (topButton == null) return false;
            if (topButton.getY() < navigator.navigatorY + 62) {
                for (int i = 0; i < 20; i++) {
                    if (topButton.getY() < navigator.navigatorY + 62) {
                        for (Button button : options) {
                            button.setY(button.getY() + 1);
                            moveAll(button, 0, 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveUp();
                    }
                }
            }
        } else if (amount < 0) {
            Button bottomButton = getVeryBottomButton();
            if (bottomButton == null) return false;
            if (bottomButton.getY() + bottomButton.getHeight() > navigator.navigatorY + navigator.navigatorHeight - 27) {
                for (int i = 0; i < 20; i++) {
                    if (bottomButton.getY() + bottomButton.getHeight() > navigator.navigatorY + navigator.navigatorHeight - 27) {
                        for (Button button : options) {
                            button.setY(button.getY() - 1);
                            moveAll(button, 0, -1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveDown();
                    }
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void tick() {
        if (fade) {
            fadeAmount += 0.1f;
            fadeAmount = MathHelper.clamp(fadeAmount, 0, 1);
            if (fadeAmount == 1)
                fade = false;
        }
        if (scrollbar.getViewportHeight() > scrollbar.getContentHeight())
            while (options.get(0).getY() < navigator.navigatorY + 62) {
                for (Button button : options) {
                    button.setY(button.getY() + 1);
                    moveAll(button, 0, 1);
                }
                if (scrollbar != null)
                    scrollbar.moveUp();
            }
        if (movingScrollbar) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar();
            else
                movingScrollbar = false;
        }
        super.tick();
    }

    private void moveScrollbar() {
        float mouseY = MouseHelper.INSTANCE.getMouseY();
        float scrollBarHoldingArea = scrollbar.getY() + (scrollbar.getHeight() / 2.f);
        float dif = mouseY - scrollBarHoldingArea;
        if (dif > 1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() + scrollbar.getHeight() < scrollbar.getViewportY() + scrollbar.getViewportHeight()) {
                    scrollbar.moveDown();
                    for (Button button : options) {
                        button.setY(button.getY() - 1);
                        moveAll(button, 0, -1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (Button button : options) {
                        button.setY(button.getY() + 1);
                        moveAll(button, 0, 1);
                    }
                }
            }
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        navigator.resize(client, width, height);
        super.resize(client, width, height);
    }

    public void moveAll(Button button, float x, float y) {
        button.getChildren().forEach(button1 -> {
            button1.move(x, y);
            if (button1.hasChildren())
                moveAll(button1, x, y);
        });
    }

    public Button getVeryBottomButton() {
        if (options.size() == 0)
            return null;
        Button b = options.get(options.size() - 1);
        while (b.hasChildren() && b.isOpen()) {
            b = b.getChildren().get(b.getChildren().size() - 1);
        }
        return b;
    }

    private boolean isHoveredToggleButton() {
        return Render2DHelper.INSTANCE.isHovered(navigator.navigatorX + 4, navigator.navigatorY + navigator.navigatorHeight - 24, navigator.navigatorWidth - 8, 20);
    }
}