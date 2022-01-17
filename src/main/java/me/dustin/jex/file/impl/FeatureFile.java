package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.OptionManager;
import me.dustin.jex.feature.option.types.ColorOption;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

    public void saveButton() {
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
                if (feature.getState() != state)
                    feature.setState(state);
                feature.setKey(key);
                feature.setVisible(visible);
            }
        });
    }
}
