package me.dustin.jex.file;

import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.command.CommandManager;
import me.dustin.jex.gui.click.ClickGui;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ClientSettingsFile {

    private static String fileName = "Settings.json";

    public static void write() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("prefix", CommandManager.INSTANCE.getPrefix());
        jsonObject.addProperty("main-menu-bg", JexTitleScreen.background);
        jsonObject.addProperty("auto-save", JexClient.INSTANCE.isAutoSaveEnabled());
        jsonObject.addProperty("gui-click-sounds", ClickGui.doesPlayClickSound());

        ArrayList<String> stringList = new ArrayList<>();
        for (String s : JsonHelper.INSTANCE.prettyGson.toJson(jsonObject).split("\n")) {
            stringList.add(s);
        }

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
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonObject.class);
            in.close();
            CommandManager.INSTANCE.setPrefix(object.get("prefix").getAsString());
            JexTitleScreen.background = object.get("main-menu-bg").getAsInt();
            JexClient.INSTANCE.setAutoSave(object.get("auto-save").getAsBoolean());
            ClickGui.setDoesPlayClickSound(object.get("gui-click-sounds").getAsBoolean());
        } catch (Exception e) {

        }
    }

}
