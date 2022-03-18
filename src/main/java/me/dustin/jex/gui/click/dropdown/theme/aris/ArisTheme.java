package me.dustin.jex.gui.click.dropdown.theme.aris;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.gui.click.dropdown.theme.DropdownTheme;
import me.dustin.jex.gui.click.dropdown.theme.aris.window.ArisConfigWindow;
import me.dustin.jex.gui.click.dropdown.theme.aris.window.ArisDropdownWindow;
import me.dustin.jex.gui.click.dropdown.theme.aris.window.ArisThemeWindow;
import org.apache.commons.lang3.StringUtils;

public class ArisTheme extends DropdownTheme {

    public ArisTheme() {
        super("Aris");
        setButtonOffset(2);
        setButtonWidthOffset(4);
        setButtonSize(16);
        setTopBarSize(18);
        setOptionOffset(2);
        setOptionButtonOffset(2);
        setOptionWidthOffset(4);
        setBottomOffset(4);
        setTopBarOffset(2);
        setResizeBoxSize(2);
    }

    @Override
    public void init() {
        if (windows.isEmpty()) {
            int i = 0;
            for (Feature.Category value : Feature.Category.values()) {
                ArisDropdownWindow dropdownWindow = new ArisDropdownWindow(this, StringUtils.capitalize(value.name().toLowerCase()), 2 + (i * 95), 8, 90, 250);
                dropdownWindow.init();
                windows.add(dropdownWindow);
                i++;
            }
            ArisConfigWindow configWindow = new ArisConfigWindow(this, 2 + (i * 95), 8, 90, 60);
            configWindow.init();
            windows.add(configWindow);
            i++;
            ArisThemeWindow themeWindow = new ArisThemeWindow(this, 2 + (i * 95), 8, 90, 60);
            themeWindow.init();
            windows.add(themeWindow);
            i++;
        }
        super.init();
    }
}
