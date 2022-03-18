package me.dustin.jex.gui.click.dropdown.theme.aris.window;

import me.dustin.jex.JexClient;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.ClientSettingsFile;
import me.dustin.jex.file.impl.FeatureFile;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.aris.ArisDropdownToggleButton;
import me.dustin.jex.helper.render.Scrollbar;

public class ArisConfigWindow extends ArisDropdownWindow {
    public ArisConfigWindow(DropdownTheme theme, float x, float y, float width, float maxHeight) {
        super(theme, "Config", x, y, width, maxHeight);
    }

    @Override
    public void init() {
        int buttonCount = 0;
        ArisDropdownToggleButton saveButton = new ArisDropdownToggleButton(this, "Save",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getResizeBoxSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).saveButton();
        });
        saveButton.setToggled(true);
        this.getButtons().add(saveButton);
        buttonCount++;
        ArisDropdownToggleButton loadButton = new ArisDropdownToggleButton(this, "Load",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            ConfigManager.INSTANCE.get(FeatureFile.class).read();
        });
        loadButton.setToggled(true);
        this.getButtons().add(loadButton);
        buttonCount++;
        ArisDropdownToggleButton autoSaveButton = new ArisDropdownToggleButton(this, "AutoSave",this.getX() + getTheme().getButtonWidthOffset(), this.getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (buttonCount * (getTheme().getButtonSize() + getTheme().getButtonOffset())), this.getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
            JexClient.INSTANCE.setAutoSave(!JexClient.INSTANCE.isAutoSaveEnabled());
            this.getButtons().get(2).setName("AutoSave");
            ((ArisDropdownToggleButton)this.getButtons().get(2)).setToggled(JexClient.INSTANCE.isAutoSaveEnabled());
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
