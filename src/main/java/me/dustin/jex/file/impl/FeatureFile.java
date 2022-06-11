package me.dustin.jex.file.impl;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.property.PropertyManager;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.helper.file.YamlHelper;
import me.dustin.jex.helper.render.Render2DHelper;

import java.awt.*;
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
            featureMap.put("state", feature.getState());
            featureMap.put("visible", feature.isVisible());
            if (PropertyManager.INSTANCE.hasProperties(feature.getClass())) {
                Map<String, Object> optionsMap = new HashMap<>();
                for (Property<?> property : PropertyManager.INSTANCE.get(feature.getClass())) {
                    if (property.getDefaultValue() instanceof Enum<?>) {
                        Property<Enum<?>> enumProperty = (Property<Enum<?>>) property;
                        optionsMap.put(property.getName(), enumProperty.value().name());
                    } else if (property.getDefaultValue() instanceof Color) {
                        Property<Color> colorProperty = (Property<Color>) property;
                        optionsMap.put(property.getName(), Integer.toHexString(colorProperty.value().getRGB()).substring(2));
                    } else
                        optionsMap.put(property.getName(), property.value());
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
            featureMap.put("state", feature.getState());
            featureMap.put("visible", feature.isVisible());
            if (PropertyManager.INSTANCE.hasProperties(feature.getClass())) {
                Map<String, Object> optionsMap = new HashMap<>();
                for (Property<?> property : PropertyManager.INSTANCE.get(feature.getClass())) {
                    if (property.getDefaultValue() instanceof Enum<?>) {
                        Property<Enum<?>> enumProperty = (Property<Enum<?>>) property;
                        optionsMap.put(property.getName(), enumProperty.value().name());
                    } else if (property.getDefaultValue() instanceof Color) {
                        Property<Color> colorProperty = (Property<Color>) property;
                        optionsMap.put(property.getName(), Integer.toHexString(colorProperty.value().getRGB()).substring(2));
                    } else
                     optionsMap.put(property.getName(), property.value());
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
                boolean visible = (boolean) featureValues.get("visible");
                boolean state = (boolean) featureValues.get("state");
                Map<String, Object> optionsMap = (Map<String, Object>) featureValues.get("Options");
                if (optionsMap != null)
                    optionsMap.forEach((s1, o1) -> {
                        Property property = PropertyManager.INSTANCE.get(feature.getClass(), s1);
                        if (property == null) {
                            JexClient.INSTANCE.getLogger().error("Could not find option: " + s1 + " for feature: " + feature.getName());
                            return;
                        }
                        try {
                            if (property.getDefaultValue() instanceof Enum<?>) {
                                property.setEnumValue((String) o1);
                            } else if (property.getDefaultValue() instanceof Color) {
                                Property<Color> colorProperty = (Property<Color>) property;
                                String str = "ff" + String.valueOf(o1);
                                Color color = Render2DHelper.INSTANCE.hex2Rgb(str);
                                colorProperty.setValue(color);
                            } else if (property.getDefaultValue() instanceof Float) {
                                Property<Float> floatProperty = (Property<Float>) property;
                                floatProperty.setValue((float) (double) o1);
                            } else if (property.getDefaultValue() instanceof Long) {
                                Property<Long> longProperty = (Property<Long>) property;
                                longProperty.setValue((long)(int) o1);
                            } else
                                property.setValue(o1);
                        } catch (Exception e) {
                            JexClient.INSTANCE.getLogger().error("Option error: " + property.getName() + " " + feature.getName());
                            e.printStackTrace();
                        }
                    });
                if (feature.getState() != state)
                    feature.setState(state);
                feature.setVisible(visible);
            }
        });
    }
}
