package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.feature.mod.impl.render.Search;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ConfigFile.CFG(fileName = "Search.yml", folder = "config")
public class SearchFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        Search.getBlocks().keySet().forEach(block -> {
            Map<String, Object> blockData = new HashMap<>();
            blockData.put("mod", Registry.BLOCK.getKey(block).getNamespace());
            blockData.put("color", Integer.toHexString(Search.getBlocks().get(block)));
            String blockName = Registry.BLOCK.getKey(block).toString();
            if (blockName.contains(":"))
                blockName = blockName.split(":")[1];
            yamlMap.put(blockName, blockData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        convertJson();
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedYaml == null || parsedYaml.isEmpty())
            return;
        parsedYaml.forEach((s, o) -> {
            Map<String, Object> blockData = (Map<String, Object>)o;
            String mod = (String)blockData.get("mod");
            int color = Render2DHelper.INSTANCE.hex2Rgb((String)blockData.get("color")).getRGB();
            Optional<Block> block = Registry.BLOCK.getOptional(new ResourceLocation(mod, s));
            Search.getBlocks().put(block.get(), color);
        });
    }

    public void convertJson() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Search.json");
        try {
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String blockID = object.get("blockID").getAsString();
                Optional<Block> block = Registry.BLOCK.getOptional(new ResourceLocation(blockID));
                int color = Render2DHelper.INSTANCE.hex2Rgb(object.get("color").getAsString()).getRGB();
                Search.getBlocks().put(block.get(), color);
            }
            file.delete();
            write();
        } catch (Exception e) {

        }
    }

}
