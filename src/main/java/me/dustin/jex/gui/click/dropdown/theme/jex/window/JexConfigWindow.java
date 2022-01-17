package me.dustin.jex.gui.click.dropdown.theme.jex.window;

import me.dustin.jex.JexClient;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Scissor;
import me.dustin.jex.helper.render.Scrollbar;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class JexConfigWindow extends JexDropdownWindow {
    public JexConfigWindow(DropdownTheme dropdownTheme, float x, float y, float width, float maxHeight) {
        super(dropdownTheme, "Config", x, y, width, maxHeight);
    }

    @Override
    public void init() {
        int buttonCount = 0;
        this.getButtons().add(new DropdownButton(this, "Save",this.getX() + 1, this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).saveButton();
        }));
        buttonCount++;
        this.getButtons().add(new DropdownButton(this, "Load",this.getX() + 1, this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).read();
        }));
        buttonCount++;
        this.getButtons().add(new DropdownButton(this, "AutoSave: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? Formatting.GREEN + "ON" : Formatting.RED + "OFF"),this.getX() + 1, this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - 2, getTheme().getButtonSize(), unused -> {
            JexClient.INSTANCE.setAutoSave(!JexClient.INSTANCE.isAutoSaveEnabled());
            this.getButtons().get(2).setName("Auto-Save: " + (JexClient.INSTANCE.isAutoSaveEnabled() ? Formatting.GREEN + "ON" : Formatting.RED + "OFF"));
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        }));
        buttonCount++;
        this.setPrevHeight(getHeight());
        this.setScrollbar(new Scrollbar(getX() + getWidth() - 1, getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset(), 2, getHeight() - getTheme().getResizeBoxSize() - getTheme().getTopBarSize() - getTheme().getTopBarOffset(), getHeight() - getTheme().getTopBarSize() - getTheme().getTopBarOffset() * 2, getHeight(), 0xffffffff));
        super.init();
    }
}
