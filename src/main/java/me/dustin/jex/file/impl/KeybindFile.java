package me.dustin.jex.file.impl;

import me.dustin.jex.feature.keybind.Keybind;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.YamlHelper;

import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Keybinds.yml", folder = "config")
public class KeybindFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        Keybind.getKeybinds().forEach(keybind -> {
            Map<String, Object> bindMap = new HashMap<>();
            bindMap.put("key", keybind.key());
            bindMap.put("isJexCommand", keybind.isJexCommand());
            yamlMap.put(keybind.command(), bindMap);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        if (!getFile().exists()) return;
        Map<String, Object> binds = YamlHelper.INSTANCE.readFile(getFile());
        if (binds == null || binds.isEmpty())
            return;
        Keybind.getKeybinds().clear();
        binds.forEach((command, map) -> {
            Map<String, Object> bind = (Map<String, Object>)map;
            int key = (int) bind.get("key");
            boolean isJexCommand = (boolean) bind.get("isJexCommand");
            Keybind.add(key, command, isJexCommand);
        });
    }
}
