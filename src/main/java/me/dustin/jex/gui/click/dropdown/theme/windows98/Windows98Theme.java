package me.dustin.jex.gui.click.dropdown.theme.windows98;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.aris.window.ArisConfigWindow;
import me.dustin.jex.gui.click.dropdown.theme.aris.window.ArisDropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.aris.window.ArisThemeWindow;
import me.dustin.jex.gui.click.dropdown.theme.windows98.window.Windows98ConfigWindow;
import me.dustin.jex.gui.click.dropdown.theme.windows98.window.Windows98DropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.windows98.window.Windows98ThemeWindow;
import org.apache.commons.lang3.StringUtils;

public class Windows98Theme extends DropdownTheme {
    public Windows98Theme() {
        super("Windows98");
        setButtonOffset(0);
        setButtonWidthOffset(1);
        setButtonSize(14);
        setTopBarSize(12);
        setOptionOffset(1);
        setOptionButtonOffset(0);
        setOptionWidthOffset(2);
        setBottomOffset(1);
        setTopBarOffset(0);
        setResizeBoxSize(2);
    }

    @Override
    public void init() {
        if (windows.isEmpty()) {
            int i = 0;
            for (Category value : Category.values()) {
                Windows98DropdownWindow dropdownWindow = new Windows98DropdownWindow(this, StringUtils.capitalize(value.name().toLowerCase()), 2 + (i * 95), 8, 90, 250);
                dropdownWindow.init();
                windows.add(dropdownWindow);
                i++;
            }
            Windows98ConfigWindow configWindow = new Windows98ConfigWindow(this, 2 + (i * 95), 8, 90, 60);
            configWindow.init();
            windows.add(configWindow);
            i++;
            Windows98ThemeWindow themeWindow = new Windows98ThemeWindow(this, 2 + (i * 95), 8, 90, 60);
            themeWindow.init();
            windows.add(themeWindow);
            i++;
        }
        super.init();
    }
}
