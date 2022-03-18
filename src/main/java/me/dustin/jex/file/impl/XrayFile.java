package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.feature.mod.impl.world.xray.Xray;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConfigFile.CFG(fileName = "Xray.yml", folder = "config")
public class XrayFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        Xray.blockList.forEach(block -> {
            Map<String, Object> blockData = new HashMap<>();
            blockData.put("mod", Registry.BLOCK.getId(block).getNamespace());
            String blockName = Registry.BLOCK.getId(block).toString();
            if (blockName.contains(":"))
                blockName = blockName.split(":")[1];
            yamlMap.put(blockName, blockData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        convertJson();
        Map<String, Object> parsedyaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedyaml == null || parsedyaml.isEmpty())
            return;
        parsedyaml.forEach((s, o) -> {
            Map<String, Object> blockData = (Map<String, Object>)o;
            String mod = (String)blockData.get("mod");
            Optional<Block> block = Registry.BLOCK.getOrEmpty(new Identifier(mod, s));
            Xray.blockList.add(block.get());
        });
    }

    public void convertJson() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Xray.json");
        try {
            Xray.blockList.clear();
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String blockName = object.get("block").getAsString();
                Block block = Registry.BLOCK.get(new Identifier(blockName));
                Xray.blockList.add(block);
            }
            file.delete();
            write();
        } catch (Exception e) {

        }
    }
}
