package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import me.dustin.jex.feature.mod.impl.render.Trail;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Trails.yml", folder = "config")
public class TrailsFile extends ConfigFile {

    @Override
    public void read() {
        convertJson();
        Map<String, Object> parsedyaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedyaml == null || parsedyaml.isEmpty())
            return;
        parsedyaml.forEach((s, o) -> {
            Map<String, Object> particleData = (Map<String, Object>) o;
            Identifier id = new Identifier((String)particleData.get("mod"), s);
            Trail.getParticles().add(Registry.PARTICLE_TYPE.get(id));
        });
    }

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        Trail.getParticles().forEach(particleType -> {
            Map<String, Object> particleData = new HashMap<>();
            Identifier id = Registry.PARTICLE_TYPE.getId(particleType);
            particleData.put("mod", id.getNamespace() == null ? "minecraft" : id.getNamespace());
            yamlMap.put(id.getPath(), particleData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    public void convertJson() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Trails.json");
        if (!file.exists())
            return;
        Trail.getParticles().clear();
        try {
            StringBuilder stringBuffer = new StringBuilder("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(stringBuffer.toString(), JsonArray.class);
            for (int i = 0; i < array.size(); i++) {
                String particle = array.get(i).getAsString();
                Trail.getParticles().add(Registry.PARTICLE_TYPE.get(new Identifier(particle)));
            }
            in.close();
            file.delete();
            write();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
