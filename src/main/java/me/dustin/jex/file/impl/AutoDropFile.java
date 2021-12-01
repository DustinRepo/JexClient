package me.dustin.jex.file.impl;

import me.dustin.jex.feature.mod.impl.player.AutoDrop;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.YamlHelper;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConfigFile.CFG(fileName = "AutoDrop.yml", folder = "config")
public class AutoDropFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        AutoDrop.INSTANCE.getItems().forEach(item -> {
            Map<String, Object> blockData = new HashMap<>();
            blockData.put("mod", Registry.ITEM.getId(item).getNamespace());
            String itemName = Registry.ITEM.getId(item).toString();
            if (itemName.contains(":"))
                itemName = itemName.split(":")[1];
            yamlMap.put(itemName, blockData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        Map<String, Object> parsedyaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedyaml == null || parsedyaml.isEmpty())
            return;
        AutoDrop.INSTANCE.getItems().clear();
        parsedyaml.forEach((s, o) -> {
            Map<String, Object> blockData = (Map<String, Object>)o;
            String mod = (String)blockData.get("mod");
            Optional<Item> item = Registry.ITEM.getOrEmpty(new Identifier(mod, s));
            item.ifPresent(value -> AutoDrop.INSTANCE.getItems().add(value));
        });
    }
}
