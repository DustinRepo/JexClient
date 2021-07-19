package me.dustin.jex.helper.file.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.OptionManager;
import me.dustin.jex.feature.option.types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FeatureFile {

    private static String fileName = "Features.json";
    private static boolean firstLoad = true;

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
                            if (option instanceof BoolOption) {
                                BoolOption boolOption = (BoolOption) option;
                                optionObject.addProperty("value", boolOption.getValue());
                            } else if (option instanceof FloatOption) {
                                FloatOption boolOption = (FloatOption) option;
                                optionObject.addProperty("value", boolOption.getValue());
                            } else if (option instanceof IntOption) {
                                IntOption boolOption = (IntOption) option;
                                optionObject.addProperty("value", boolOption.getValue());
                            } else if (option instanceof StringArrayOption) {
                                StringArrayOption boolOption = (StringArrayOption) option;
                                optionObject.addProperty("value", boolOption.getValue());
                            } else if (option instanceof StringOption) {
                                StringOption boolOption = (StringOption) option;
                                optionObject.addProperty("value", boolOption.getValue());
                            } else if (option instanceof ColorOption) {
                                ColorOption boolOption = (ColorOption) option;
                                optionObject.addProperty("value", Integer.toHexString(boolOption.getValue()));
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

    private static boolean contains(String[] values, String value) {
        for (String s : values) {
            if (s.equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

}
