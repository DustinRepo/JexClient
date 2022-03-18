package me.dustin.jex.gui.click.dropdown.theme.aris.window;

import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.GuiFile;
import me.dustin.jex.file.impl.GuiThemeFile;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.aris.ArisDropdownToggleButton;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Scrollbar;

public class ArisThemeWindow extends ArisDropdownWindow {
    public ArisThemeWindow(DropdownTheme theme, float x, float y, float width, float maxHeight) {
        super(theme, "Theme", x, y, width, maxHeight);
    }

    @Override
    public void init() {
        int i = 0;
        for (DropdownTheme theme : DropDownGui.getThemes()) {
            ArisDropdownToggleButton toggleButton = new ArisDropdownToggleButton(this, theme.getName(), getX() + getTheme().getButtonWidthOffset(), getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset() + (i * (getTheme().getButtonSize() + getTheme().getButtonOffset())), getWidth() - getTheme().getButtonWidthOffset() * 2, getTheme().getButtonSize(), unused -> {
                ConfigManager.INSTANCE.get(GuiFile.class).write();
                DropDownGui.setCurrentTheme(theme);
                ConfigManager.INSTANCE.get(GuiThemeFile.class).write();
                Wrapper.INSTANCE.getMinecraft().setScreen(new DropDownGui());
            });
            toggleButton.setToggled(theme == DropDownGui.getCurrentTheme());
            getButtons().add(toggleButton);
            i++;
        }
        this.setPrevHeight(getHeight());
        this.setScrollbar(new Scrollbar(getX() + getWidth() - 2, getY() + getTheme().getTopBarSize() + getTheme().getTopBarOffset(), 2, getHeight() - getTheme().getResizeBoxSize() - getTheme().getTopBarSize() - getTheme().getTopBarOffset(), getHeight() - getTheme().getTopBarSize() - getTheme().getTopBarOffset() * 2, getHeight(), 0xffffffff));
        super.init();
    }
}
