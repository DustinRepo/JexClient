package me.dustin.jex.file.impl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.OptionManager;
import me.dustin.jex.feature.option.types.*;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;

@ConfigFile.CFG(fileName = "Features.yml", folder = "config")
public class FeatureFile extends ConfigFile {

    @Override
    public void write() {
        if (!JexClient.INSTANCE.isAutoSaveEnabled())
            return;
        Map<String, Object> yamlMap = new HashMap<>();
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            Map<String, Object> featureMap = new HashMap<>();
            featureMap.put("key", feature.getKey());
            featureMap.put("state", feature.getState());
            featureMap.put("visible", feature.isVisible());
            if (OptionManager.INSTANCE.hasOption(feature)) {
                Map<String, Object> optionsMap = new HashMap<>();
                for (Option option : OptionManager.get().getOptions()) {
                    if (option.getFeature() == feature) {
                        optionsMap.put(option.getName(), option instanceof ColorOption ? Integer.toHexString((int)option.getRawValue()) : option.getRawValue());
                    }
                }
                featureMap.put("Options", optionsMap);
            }
            yamlMap.put(feature.getName(), featureMap);
        }

        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    public static void saveButton() {
        Map<String, Object> yamlMap = new HashMap<>();
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            Map<String, Object> featureMap = new HashMap<>();
            featureMap.put("key", feature.getKey());
            featureMap.put("state", feature.getState());
            featureMap.put("visible", feature.isVisible());
            if (OptionManager.INSTANCE.hasOption(feature)) {
                Map<String, Object> optionsMap = new HashMap<>();
                for (Option option : OptionManager.get().getOptions()) {
                    if (option.getFeature() == feature) {
                        optionsMap.put(option.getName(), option instanceof ColorOption ? Integer.toHexString((int)option.getRawValue()) : option.getRawValue());
                    }
                }
                featureMap.put("Options", optionsMap);
            }
            yamlMap.put(feature.getName(), featureMap);
        }

        YamlHelper.INSTANCE.writeFile(yamlMap, ConfigManager.INSTANCE.get(FeatureFile.class).getFile());
    }

    @Override
    public void read() {
        convertFromJson();
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedYaml == null || parsedYaml.isEmpty())
            return;
        parsedYaml.forEach((s, o) -> {
            Feature feature = Feature.get(s);
            if (feature == null) {
                JexClient.INSTANCE.getLogger().error("Could not find feature: " + s);
                return;
            }
            if (o instanceof Map) {
                Map<String, Object> featureValues = (Map<String, Object>) o;
                int key = (int) featureValues.get("key");
                boolean visible = (boolean) featureValues.get("visible");
                boolean state = (boolean) featureValues.get("state");
                Map<String, Object> optionsMap = (Map<String, Object>) featureValues.get("Options");
                if (optionsMap != null)
                    optionsMap.forEach((s1, o1) -> {
                        Option option = OptionManager.get().getOption(s1, feature);
                        if (option == null) {
                            JexClient.INSTANCE.getLogger().error("Could not find option: " + s1 + " for feature: " + feature);
                            return;
                        }
                        option.parseValue(String.valueOf(o1));
                    });
                feature.setState(state);
                feature.setKey(key);
                feature.setVisible(visible);
            }
        });
    }

    public void convertFromJson() {
        try {
            File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Features.json");
            if (!file.exists())
                return;
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            if (array != null)
                for (int i = 0; i < array.size(); i++) {
                    JsonObject object = array.get(i).getAsJsonObject();
                    String name = object.get("name").getAsString();
                    Feature feature = Feature.get(name);
                    if (feature != null) {
                        feature.setKey(object.get("key").getAsInt());
                        feature.setVisible(object.get("visible").getAsBoolean());
                        if (feature.getState() != object.get("state").getAsBoolean())
                            feature.setState(object.get("state").getAsBoolean());
                    } else {
                        JexClient.INSTANCE.getLogger().error("Could not find Module " + name);
                    }
                    JsonArray objectArray = null;


                    if (OptionManager.get().hasOption(feature) && object.has("Properties"))
                        objectArray = object.get("Properties").getAsJsonArray();

                    if (objectArray != null)
                        for (int j = 0; j < objectArray.size(); j++) {
                            JsonObject newObject = objectArray.get(j).getAsJsonObject();
                            String opName = newObject.get("name").getAsString();
                            String valueString = newObject.get("value").getAsString();
                            Option option = OptionManager.get().getOption(opName, feature);
                            if (option != null) {
                                option.parseValue(valueString);
                            }
                        }
                }
            file.delete();
            boolean autoSave = JexClient.INSTANCE.isAutoSaveEnabled();
            JexClient.INSTANCE.setAutoSave(true);
            write();
            JexClient.INSTANCE.setAutoSave(autoSave);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
