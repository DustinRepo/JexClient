package me.dustin.jex.gui.click.dropdown.theme.jex;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.theme.jex.window.JexThemeWindow;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.jex.window.JexConfigWindow;
import me.dustin.jex.gui.click.dropdown.theme.jex.window.JexDropdownWindow;
import org.apache.commons.lang3.StringUtils;

public class JexTheme extends DropdownTheme {
    public JexTheme() {
        super("Default");
        setTopBarSize(15);
        setResizeBoxSize(2);
        setButtonSize(12);
        setButtonOffset(0);
        setTopBarOffset(0);
        setOptionOffset(2);
        setOptionWidthOffset(1);
        setBottomOffset(1);
    }

    @Override
    public void init() {
        if (windows.isEmpty()) {
            int i = 0;
            for (Feature.Category value : Feature.Category.values()) {
                JexDropdownWindow dropdownWindow = new JexDropdownWindow(this, StringUtils.capitalize(value.name().toLowerCase()), 2 + (i * 95), 8, 90, 250);
                dropdownWindow.init();
                windows.add(dropdownWindow);
                i++;
            }
            JexConfigWindow configWindow = new JexConfigWindow(this, 2 + (i * 95), 8, 90, 60);
            configWindow.init();
            windows.add(configWindow);
            i++;
            JexThemeWindow themeWindow = new JexThemeWindow(this, 2 + (i * 95), 8, 90, 60);
            themeWindow.init();
            windows.add(themeWindow);
            i++;
        }
        super.init();
    }
}
