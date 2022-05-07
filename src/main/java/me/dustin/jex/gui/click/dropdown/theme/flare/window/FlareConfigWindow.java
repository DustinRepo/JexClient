package me.dustin.jex.gui.click.dropdown.theme.flare.window;

import me.dustin.jex.JexClient;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.impl.button.DropdownButton;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.flare.FlareDropdownToggleButton;
import me.dustin.jex.helper.render.Scrollbar;

public class FlareConfigWindow extends FlareDropdownWindow {
    public FlareConfigWindow(DropdownTheme theme, float x, float y, float width, float maxHeight) {
        super(theme, "Config", x, y, width, maxHeight);
    }

    @Override
    public void init() {
        int buttonCount = 0;
        DropdownButton saveButton = new DropdownButton(this, "Save",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getResizeBoxSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).saveButton();
        });
        saveButton.setBackgroundColor(0x00ffffff);
        saveButton.setTextColor(-1);
        saveButton.setCenterText(false);
        this.getButtons().add(saveButton);
        buttonCount++;
        DropdownButton loadButton = new DropdownButton(this, "Load",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).read();
        });
        loadButton.setBackgroundColor(0x00ffffff);
        loadButton.setTextColor(-1);
        loadButton.setCenterText(false);
        this.getButtons().add(loadButton);
        buttonCount++;
        FlareDropdownToggleButton autoSaveButton = new FlareDropdownToggleButton(this, "AutoSave",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            JexClient.INSTANCE.setAutoSave(!JexClient.INSTANCE.isAutoSaveEnabled());
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
            ((FlareDropdownToggleButton)this.getButtons().get(2)).setToggled(JexClient.INSTANCE.isAutoSaveEnabled());
        });
        autoSaveButton.setCenterText(false);
        this.getButtons().add(autoSaveButton);
        buttonCount++;
        this.setPrevHeight(getHeight());
        this.setScrollbar(new Scrollbar(getX() + getWidth() - 1, getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset(), 2, getHeight() - getTheme().getResizeBoxSize() - getTheme().getTopBarSize() - getTheme().getTopBarOffset(), getHeight() - getTheme().getTopBarSize() - getTheme().getTopBarOffset() * 2, getHeight(), 0xffffffff));
        super.init();
    }
}
