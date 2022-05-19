package me.dustin.jex.gui.click.dropdown.theme.flare;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.theme.flare.window.FlareThemeWindow;
import me.dustin.jex.gui.click.dropdown.theme.jex.window.JexThemeWindow;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.flare.window.FlareConfigWindow;
import me.dustin.jex.gui.click.dropdown.theme.flare.window.FlareDropdownWindow;
import org.apache.commons.lang3.StringUtils;

public class FlareTheme extends DropdownTheme {
    public FlareTheme() {
        super("Flare");
        setButtonOffset(0);
        setButtonWidthOffset(1);
        setButtonSize(14);
        setTopBarSize(14);
        setOptionOffset(4);
        setOptionWidthOffset(4);
        setBottomOffset(1);
        setTopBarOffset(2);
        setResizeBoxSize(2);
    }

    @Override
    public void init() {
        if (windows.isEmpty()) {
            int i = 0;
            for (Category value : Category.values()) {
                FlareDropdownWindow dropdownWindow = new FlareDropdownWindow(this, StringUtils.capitalize(value.name().toLowerCase()), 2 + (i * 95), 8, 90, 250);
                dropdownWindow.init();
                windows.add(dropdownWindow);
                i++;
            }
            FlareConfigWindow configWindow = new FlareConfigWindow(this, 2 + (i * 95), 8, 90, 60);
            configWindow.init();
            windows.add(configWindow);
            i++;
            FlareThemeWindow themeWindow = new FlareThemeWindow(this, 2 + (i * 95), 8, 90, 60);
            themeWindow.init();
            windows.add(themeWindow);
            i++;
        }
        super.init();
    }

}
