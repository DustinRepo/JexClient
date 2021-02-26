package me.dustin.jex.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.ModuleManager;
import me.dustin.jex.option.Option;
import me.dustin.jex.option.OptionManager;
import me.dustin.jex.option.types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ModuleFile {

    private static String fileName = "Modules.json";
    private static boolean firstLoad = true;

    public static void write() {
        JsonArray jsonArray = new JsonArray();
        for (Module module : ModuleManager.INSTANCE.getModules().values()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", module.getName());
            jsonObject.addProperty("key", module.getKey());
            jsonObject.addProperty("state", module.getState());
            jsonObject.addProperty("visible", module.isVisible());
            if (OptionManager.get().hasOption(module)) {
                JsonArray options = new JsonArray();
                for (Option option : OptionManager.get().getOptions()) {
                    if (option.getModule() == module) {
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
                            System.out.println(module.getName() + " " + option.getName());
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
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(ModFileHelper.INSTANCE.getJexDirectory(), fileName).getPath()), "UTF8"));
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
                    Module module = Module.get(name);
                    if (module != null) {
                        module.setKey(object.get("key").getAsInt());
                        module.setVisible(object.get("visible").getAsBoolean());
                        module.setState(object.get("state").getAsBoolean());
                    } else {
                        System.out.println("Could not find Module " + name);
                    }
                    JsonArray objectArray = null;


                    if (OptionManager.get().hasOption(module) && object.has("Properties"))
                        objectArray = object.get("Properties").getAsJsonArray();

                    if (objectArray != null)
                        for (int j = 0; j < objectArray.size(); j++) {
                            JsonObject newObject = objectArray.get(j).getAsJsonObject();
                            String opName = newObject.get("name").getAsString();
                            String valueString = newObject.get("value").getAsString();
                            Option option = OptionManager.get().getOption(opName, module);
                            if (option != null) {
                                option.parseValue(valueString);
                            }
                        }
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
