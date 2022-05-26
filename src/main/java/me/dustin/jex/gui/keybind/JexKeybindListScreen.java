package me.dustin.jex.gui.keybind;

import me.dustin.jex.feature.keybind.Keybind;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.KeybindFile;
import me.dustin.jex.gui.account.impl.AccountButton;
import me.dustin.jex.gui.keybind.impl.JexKeybindButton;
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

import java.util.ArrayList;

public class JexKeybindListScreen extends Screen {
    private final Screen parent;
    private final ArrayList<JexKeybindButton> keybindButtonsList = new ArrayList<>();
    private ButtonWidget deleteButton;
    private ButtonWidget editButton;
    private Scrollbar scrollbar;
    private boolean movingScrollbar;
    public JexKeybindListScreen(Screen parent) {
        super(Text.literal("Keybinds"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        generateButtons();
        addDrawableChild(deleteButton = new ButtonWidget(width / 2 - 102, height - 25, 100, 20, Text.of("Delete"), button -> {
            Keybind.getKeybinds().remove(getSelected().getKeybind());
            ConfigManager.INSTANCE.get(KeybindFile.class).write();
            generateButtons();
        }));
        addDrawableChild(editButton = new ButtonWidget(width / 2 - 102, height - 50, 100, 20, Text.of("Edit"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexEditKeybindScreen(getSelected().getKeybind(), this));
        }));
        addDrawableChild(new ButtonWidget(width / 2 + 2, height - 50, 100, 20, Text.of("New"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(new JexEditKeybindScreen(null, this));
        }));
        addDrawableChild(new ButtonWidget(width / 2 + 2, height - 25, 100, 20, Text.of("Cancel"), button -> {
            Wrapper.INSTANCE.getMinecraft().setScreen(parent);
        }));

        if (!keybindButtonsList.isEmpty()) {
            float contentHeight = (keybindButtonsList.get(keybindButtonsList.size() - 1).getY() + (keybindButtonsList.get(keybindButtonsList.size() - 1).getHeight())) - keybindButtonsList.get(0).getY();
            float viewportHeight = height - 94;
            this.scrollbar = new Scrollbar((width / 2.f) + 130, 32, 3, height - 94, viewportHeight, contentHeight, ColorHelper.INSTANCE.getClientColor());
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        Render2DHelper.INSTANCE.fill(matrices, 0, 0, width, 30, 0x80000000);
        Render2DHelper.INSTANCE.fill(matrices, 0, height - 60, width, height, 0x80000000);
        FontHelper.INSTANCE.drawCenteredString(matrices, "Keybinds", width / 2.f, 12, ColorHelper.INSTANCE.getClientColor());
        Scissor.INSTANCE.cut(0, 30, width, height - 90);
        keybindButtonsList.forEach(jexKeybindButton -> jexKeybindButton.render(matrices));
        Scissor.INSTANCE.seal();
        if (scrollbar != null)
            scrollbar.render(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Render2DHelper.INSTANCE.isHovered(width / 2.f - 125, 30, 250, height - 90)) {
            for (JexKeybindButton jexKeybindButton : keybindButtonsList) {
                jexKeybindButton.setSelected(jexKeybindButton.isHovered());
            }
        }
        if (scrollbar != null && scrollbar.isHovered())
            movingScrollbar = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        deleteButton.active = getSelected() != null;
        editButton.active = getSelected() != null;
        if (movingScrollbar) {
            if (MouseHelper.INSTANCE.isMouseButtonDown(0))
                moveScrollbar();
            else
                movingScrollbar = false;
        }
        super.tick();
    }

    @Override
    public boolean mouseScrolled(double d, double e, double amount) {
        if (keybindButtonsList.isEmpty())
            return false;
        if (amount > 0) {
            JexKeybindButton topButton = keybindButtonsList.get(0);
            if (topButton == null) return false;
            if (topButton.getY() < 32) {
                for (int i = 0; i < 20; i++) {
                    if (topButton.getY() < 32) {
                        for (JexKeybindButton button : keybindButtonsList) {
                            button.setY(button.getY() + 1);
                        }
                        if (scrollbar != null)
                            scrollbar.moveUp();
                    }
                }
            }
        } else if (amount < 0) {
            JexKeybindButton bottomButton = keybindButtonsList.get(keybindButtonsList.size() - 1);
            if (bottomButton == null) return false;
            if (bottomButton.getY() + bottomButton.getHeight() > height - 62) {
                for (int i = 0; i < 20; i++) {
                    if (bottomButton.getY() + bottomButton.getHeight() > height - 62) {
                        for (JexKeybindButton button : keybindButtonsList) {
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
                    for (JexKeybindButton button : keybindButtonsList) {
                        button.setY(button.getY() - 1);
                    }
                }
            }
        } else if (dif < -1.5f) {
            for (int i = 0; i < Math.abs(dif); i++) {
                if (scrollbar.getY() > scrollbar.getViewportY()) {
                    scrollbar.moveUp();
                    for (JexKeybindButton button : keybindButtonsList) {
                        button.setY(button.getY() + 1);
                    }
                }
            }
        }
    }

    public void generateButtons() {
        keybindButtonsList.clear();
        int i = 0;
        for (Keybind keybind : Keybind.getKeybinds()) {
            keybindButtonsList.add(new JexKeybindButton(keybind, width / 2.f - 125, 32 + (i * 24), 250, 22, null));
            i++;
        }
    }

    public JexKeybindButton getSelected() {
        for (JexKeybindButton jexKeybindButton : keybindButtonsList) {
            if (jexKeybindButton.isSelected())
                return jexKeybindButton;
        }
        return null;
    }
}
