package me.dustin.jex.helper.file.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.OptionManager;
import me.dustin.jex.feature.option.types.*;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;

public class FeatureFile {

    private static String fileName = "Features.json";
    public static void write() {
        JsonArray jsonArray = new JsonArray();
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", feature.getName());
            jsonObject.addProperty("key", feature.getKey());
            jsonObject.addProperty("state", feature.getState());
            jsonObject.addProperty("visible", feature.isVisible());
            if (OptionManager.get().hasOption(feature)) {
                JsonArray options = new JsonArray();
                for (Option option : OptionManager.get().getOptions()) {
                    if (option.getFeature() == feature) {
                        try {
                            JsonObject optionObject = new JsonObject();
                            optionObject.addProperty("name", option.getName());
                            if (option instanceof BoolOption boolOption) {
                                optionObject.addProperty("value", boolOption.getValue());
                            } else if (option instanceof FloatOption floatOption) {
                                optionObject.addProperty("value", floatOption.getValue());
                            } else if (option instanceof IntOption intOption) {
                                optionObject.addProperty("value", intOption.getValue());
                            } else if (option instanceof KeybindOption keybindOption) {
                                optionObject.addProperty("value", keybindOption.getValue());
                            } else if (option instanceof StringArrayOption stringArrayOption) {
                                optionObject.addProperty("value", stringArrayOption.getValue());
                            } else if (option instanceof StringOption stringOption) {
                                optionObject.addProperty("value", stringOption.getValue());
                            } else if (option instanceof ColorOption colorOption) {
                                optionObject.addProperty("value", Integer.toHexString(colorOption.getValue()));
                            }
                            options.add(optionObject);
                        } catch (Exception e) {
                            JexClient.INSTANCE.getLogger().error(feature.getName() + " " + option.getName());
                        }
                    }
                }
                jsonObject.add("Properties", options);
                jsonArray.add(jsonObject);
            } else {
                jsonArray.add(jsonObject);
            }
        }
        ArrayList<String> stringList = new ArrayList<>(Arrays.asList(JsonHelper.INSTANCE.prettyGson.toJson(jsonArray).split("\n")));

        try {
            FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), fileName, stringList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void read() {
        try {
            File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), fileName);
            boolean portedOldConfig = false;
            if (!file.exists()) {
                file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Modules.json");
                if (!file.exists())
                    return;
                portedOldConfig = true;
            }
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "UTF8"));
            String line = null;
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
            if (portedOldConfig) {
                file.delete();
                write();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
