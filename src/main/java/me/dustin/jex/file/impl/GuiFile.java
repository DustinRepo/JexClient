package me.dustin.jex.file.impl;

import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.gui.click.dropdown.impl.window.DropdownWindow;
import me.dustin.jex.helper.file.YamlHelper;

import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Gui.yml", folder = "config")
public class GuiFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        DropDownGui.getCurrentTheme().windows.forEach(window -> {
            Map<String, Object> windowData = new HashMap<>();
            windowData.put("X", window.getX());
            windowData.put("Y", window.getY());
            windowData.put("Width", window.getWidth());
            windowData.put("Height", window.getHeight());
            windowData.put("Open", window.isOpen());
            windowData.put("Pinned", window.isPinned());
            yamlMap.put(window.getName(), windowData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedYaml == null || parsedYaml.isEmpty())
            return;
        parsedYaml.forEach((s, o) -> {
            DropdownWindow window = DropDownGui.getCurrentTheme().getWindow(s);
            if (window == null)
                return;
            Map<String, Object> windowData = (Map<String, Object>)o;
            float x = (float)(double)windowData.get("X");//yaml likes to explictly set the float options to double
            float y = (float)(double)windowData.get("Y");
            boolean open = (boolean)windowData.get("Open");
            boolean pinned = (boolean)windowData.get("Pinned");
            if (windowData.get("Width") != null) {
                float width = (float)(double)windowData.get("Width");
                float height = (float)(double)windowData.get("Height");
                window.resize(width, height);
            }
            window.move(x, y);
            window.setOpen(open);
            window.setPinned(pinned);
        });
    }

}
