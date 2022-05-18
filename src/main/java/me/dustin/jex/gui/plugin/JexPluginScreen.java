package me.dustin.jex.gui.plugin;

import me.dustin.jex.feature.plugin.JexPlugin;
import me.dustin.jex.feature.plugin.JexPluginManager;
import me.dustin.jex.gui.plugin.button.JexPluginButton;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;

public class JexPluginScreen extends Screen {
    private final Screen parent;
    public JexPluginScreen(Screen parent) {
        super(Text.of("Plugin Manager"));
        this.parent = parent;
    }

    private final ArrayList<JexPluginButton> pluginButtons = new ArrayList<>();
    private ButtonWidget disableButton;
    private ButtonWidget enableButton;
    private Scrollbar scrollbar;
    private boolean movingScrollbar;

    @Override
    protected void init() {
        pluginButtons.clear();
        int i = 0;
        for (JexPlugin plugin : JexPluginManager.INSTANCE.getPlugins()) {
            pluginButtons.add(new JexPluginButton(plugin, 5, 50 + (38 * i), 200, 36));
            i++;
        }
        ButtonWidget cancelButton = new ButtonWidget(width / 2 + 2, height - 22, 200, 20, Text.of("Cancel"), button -> Wrapper.INSTANCE.getMinecraft().setScreen(parent));
        ButtonWidget openFolderButton = new ButtonWidget(width / 2 - 202, height - 22, 200, 20, Text.of("Open Plugins Folder"), button -> Util.getOperatingSystem().open(new File(ModFileHelper.INSTANCE.getJexDirectory(), "plugins")));
        enableButton = new ButtonWidget(width / 2 - 202, height - 44, 200, 20, Text.of("Enable"), button -> JexPlugin.enable(getSelected().getJexPlugin()));
        disableButton = new ButtonWidget(width / 2 + 2, height - 44, 200, 20, Text.of("Disable"), button -> JexPlugin.disable(getSelected().getJexPlugin()));

        addDrawableChild(cancelButton);
        addDrawableChild(openFolderButton);
        addDrawableChild(enableButton);
        addDrawableChild(disableButton);

        if (!pluginButtons.isEmpty()) {
            float contentHeight = (pluginButtons.get(pluginButtons.size() - 1).getY() + (pluginButtons.get(pluginButtons.size() - 1).getHeight())) - pluginButtons.get(0).getY();
            float viewportHeight = height - 100;
            this.scrollbar = new Scrollbar(208, 50, 3, height - 100, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        Render2DHelper.INSTANCE.fill(matrices, 0, 0, width, 45, 0x60000000);
        Render2DHelper.INSTANCE.fill(matrices, 0, height - 45, width, height, 0x60000000);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Plugin Manager", width / 2.f, 5, -1);
        FontHelper.INSTANCE.draw(matrices, "Plugins: %s%d".formatted(Formatting.AQUA, pluginButtons.size()), 5, 35, -1);

        Render2DHelper.INSTANCE.fill(matrices, 2, 50, 208, height - 50, 0x70000000);
        Scissor.INSTANCE.cut(0, 50, 205, height - 100);
        pluginButtons.forEach(jexPluginButton -> jexPluginButton.render(matrices));
        Scissor.INSTANCE.seal();
        Render2DHelper.INSTANCE.fill(matrices, 210, 50, width - 5, height - 50, 0x70000000);
        JexPluginButton selected = getSelected();
        if (selected != null) {
            if (selected.getIcon() != null) {
                Render2DHelper.INSTANCE.bindTexture(selected.getIcon());
                Render2DHelper.INSTANCE.drawTexture(matrices, 215, 55, 0, 0, 64, 64, 64, 64);
            }
            FontHelper.INSTANCE.draw(matrices, "%s v%s".formatted(selected.getJexPlugin().getInfo().getName(), selected.getJexPlugin().getInfo().getVersion()), 285, 55, -1);
            FontHelper.INSTANCE.draw(matrices, "By: %s%s".formatted(Formatting.GRAY, selected.getAuthors()), 285, 66, -1);
            FontHelper.INSTANCE.draw(matrices, "Allows Disabling: " + greenTrueRedFalse(selected.getJexPlugin().getInfo().isAllowDisable()), 285, 77, -1);
            FontHelper.INSTANCE.draw(matrices, "Enabled: " + greenTrueRedFalse(selected.getJexPlugin().isEnabled()), 285, 88, -1);
            Wrapper.INSTANCE.getTextRenderer().drawTrimmed(Text.of(selected.getJexPlugin().getInfo().getDescription()), 215, 125, width - 220, -1);
        }
        enableButton.active = selected != null && selected.getJexPlugin().getInfo().isAllowDisable() && !selected.getJexPlugin().isEnabled();
        disableButton.active = selected != null && selected.getJexPlugin().getInfo().isAllowDisable() && selected.getJexPlugin().isEnabled();
        if (scrollbar != null)
            scrollbar.render(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (movingScrollbar) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar();
            else
                movingScrollbar = false;
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Render2DHelper.INSTANCE.isHovered(0, 50, 205, height - 100))
            for (JexPluginButton pluginButton : pluginButtons) {
                pluginButton.setSelected(pluginButton.isHovered());
            }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Wrapper.INSTANCE.getMinecraft().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double amount) {
        if (pluginButtons.isEmpty())
            return false;
        if (amount > 0) {
            JexPluginButton topButton = pluginButtons.get(0);
            if (topButton == null) return false;
            if (topButton.getY() < 50) {
                for (int i = 0; i < 20; i++) {
                    if (topButton.getY() < 50) {
                        for (JexPluginButton button : pluginButtons) {
                            button.setY(button.getY() + 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveUp();
                    }
                }
            }
        } else if (amount < 0) {
            JexPluginButton bottomButton = pluginButtons.get(pluginButtons.size() - 1);
            if (bottomButton == null) return false;
            if (bottomButton.getY() + bottomButton.getHeight() > height - 50) {
                for (int i = 0; i < 20; i++) {
                    if (bottomButton.getY() + bottomButton.getHeight() > height - 50) {
                        for (JexPluginButton button : pluginButtons) {
                            button.setY(button.getY() - 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveDown();
                    }
                }
            }
        }
        return false;
    }

    private void moveScrollbar() {
        float mouseY = MouseHelper.INSTANCE.getMouseY();
        float scrollBarHoldingArea = scrollbar.getY() + (scrollbar.getHeight() / 2.f);
        float dif = mouseY - scrollBarHoldingArea;
        if (dif > 1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() + scrollbar.getHeight() < scrollbar.getViewportY() + scrollbar.getViewportHeight()) {
                    scrollbar.moveDown();
                    for (JexPluginButton button : pluginButtons) {
                        button.setY(button.getY() - 1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (JexPluginButton button : pluginButtons) {
                        button.setY(button.getY() + 1);
                    }
                }
            }
        }
    }

    private String greenTrueRedFalse(boolean bl) {
        return "%s%s".formatted(bl ? Formatting.GREEN : Formatting.RED, StringUtils.capitalize(String.valueOf(bl)));
    }

    public JexPluginButton getSelected() {
        for (JexPluginButton pluginButton : pluginButtons) {
            if (pluginButton.isSelected())
                return pluginButton;
        }
        return null;
    }
}
