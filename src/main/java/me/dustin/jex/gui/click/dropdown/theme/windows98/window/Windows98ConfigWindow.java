package me.dustin.jex.gui.click.dropdown.theme.windows98.window;

import me.dustin.jex.JexClient;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.windows98.Windows98DropdownToggleButton;
import me.dustin.jex.helper.render.Scrollbar;

public class Windows98ConfigWindow extends Windows98DropdownWindow {
    public Windows98ConfigWindow(DropdownTheme theme, float x, float y, float width, float maxHeight) {
        super(theme, "Config", x, y, width, maxHeight);
    }

    @Override
    public void init() {
        int buttonCount = 0;
        Windows98DropdownToggleButton saveButton = new Windows98DropdownToggleButton(this, "Save",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getResizeBoxSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).saveButton();
        });
        saveButton.setToggled(true);
        this.getButtons().add(saveButton);
        buttonCount++;
        Windows98DropdownToggleButton loadButton = new Windows98DropdownToggleButton(this, "Load",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).read();
        });
        loadButton.setToggled(true);
        this.getButtons().add(loadButton);
        buttonCount++;
        Windows98DropdownToggleButton autoSaveButton = new Windows98DropdownToggleButton(this, "AutoSave",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            JexClient.INSTANCE.setAutoSave(!JexClient.INSTANCE.isAutoSaveEnabled());
            this.getButtons().get(2).setName("AutoSave");
            ((Windows98DropdownToggleButton)this.getButtons().get(2)).setToggled(JexClient.INSTANCE.isAutoSaveEnabled());
            ConfigManager.INSTANCE.get(ClientSettingsFile.class).write();
        });
        autoSaveButton.setToggled(JexClient.INSTANCE.isAutoSaveEnabled());
        this.getButtons().add(autoSaveButton);
        buttonCount++;
        this.setPrevHeight(getHeight());
        this.setScrollbar(new Scrollbar(getX() + getWidth() - 1, getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset(), 2, getHeight() - getTheme().getResizeBoxSize() - getTheme().getTopBarSize() - getTheme().getTopBarOffset(), getHeight() - getTheme().getTopBarSize() - getTheme().getTopBarOffset() * 2, getHeight(), 0xffffffff));
        super.init();
    }
}
