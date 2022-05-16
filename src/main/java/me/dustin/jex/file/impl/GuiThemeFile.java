package me.dustin.jex.file.impl;

import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.gui.click.dropdown.DropDownGui;
import me.dustin.jex.helper.file.YamlHelper;

import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "GuiTheme.yml", folder = "config")
public class GuiThemeFile extends ConfigFile {

    @Override
    public void read() {
        if (!getFile().exists()) {
            write();
            return;
        }
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedYaml == null)
            return;
        DropDownGui.setCurrentTheme(DropDownGui.getTheme((String)parsedYaml.get("Theme")));
    }

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("Theme", DropDownGui.getCurrentTheme().getName());
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }
}
