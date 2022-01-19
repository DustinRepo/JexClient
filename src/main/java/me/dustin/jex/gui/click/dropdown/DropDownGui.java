package me.dustin.jex.gui.click.dropdown;

import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.GuiFile;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.flare.FlareTheme;
import me.dustin.jex.gui.click.dropdown.theme.jex.JexTheme;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;

public class DropDownGui extends Screen {
    private static final ArrayList<DropdownTheme> themes = new ArrayList<>();
    private static DropdownTheme currentTheme;

    static {
        themes.add(new JexTheme());
        themes.add(new FlareTheme());
        currentTheme = themes.get(0);
    }

    public DropDownGui() {
        super(new LiteralText("ClickGui"));
    }

    @Override
    protected void init() {
        currentTheme.init();
        ConfigManager.INSTANCE.get(GuiFile.class).read();
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (Wrapper.INSTANCE.getWorld() == null)
            renderBackground(matrices);
        currentTheme.render(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        currentTheme.click(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        currentTheme.scroll(amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void tick() {
        currentTheme.tick();
        super.tick();
    }

    @Override
    public void onClose() {
        ConfigManager.INSTANCE.get(GuiFile.class).write();
        super.onClose();
    }

    public static DropdownTheme getCurrentTheme() {
        return currentTheme;
    }

    public static void setCurrentTheme(DropdownTheme currentTheme) {
        DropDownGui.currentTheme = currentTheme;
    }

    public static ArrayList<DropdownTheme> getThemes() {
        return themes;
    }

    public static DropdownTheme getTheme(String name) {
        for (DropdownTheme theme : getThemes()) {
            if (theme.getName().equalsIgnoreCase(name))
                return theme;
        }
        return getThemes().get(0);
    }
}
