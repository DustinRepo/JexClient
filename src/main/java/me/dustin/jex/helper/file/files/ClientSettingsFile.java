package me.dustin.jex.helper.file.files;

import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.addon.cape.Cape;
import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.gui.minecraft.JexPersonalizationScreen;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.TheAlteningHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ClientSettingsFile {

    private static String fileName = "Settings.json";

    public static void write() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("prefix", CommandManagerJex.INSTANCE.getPrefix());
        jsonObject.addProperty("main-menu-bg", JexTitleScreen.background);
        jsonObject.addProperty("auto-save", JexClient.INSTANCE.isAutoSaveEnabled());
        jsonObject.addProperty("gui-click-sounds", ClickGui.doesPlayClickSound());
        jsonObject.addProperty("launch-sound", JexClient.INSTANCE.playSoundOnLaunch());
        jsonObject.addProperty("personal-cape", JexPersonalizationScreen.setCape);
        jsonObject.addProperty("personal-hat", JexPersonalizationScreen.setHat);
        jsonObject.addProperty("altening-api-key", TheAlteningHelper.INSTANCE.getApiKey());

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
            CommandManagerJex.INSTANCE.setPrefix(object.get("prefix").getAsString());
            JexTitleScreen.background = object.get("main-menu-bg").getAsInt();
            JexClient.INSTANCE.setAutoSave(object.get("auto-save").getAsBoolean());
            ClickGui.setDoesPlayClickSound(object.get("gui-click-sounds").getAsBoolean());
            JexClient.INSTANCE.setPlaySoundOnLaunch(object.get("launch-sound").getAsBoolean());

            if (object.get("personal-cape") != null) {
                String capeLoc = object.get("personal-cape").getAsString();
                String hat = object.get("personal-hat").getAsString();
                File file = new File(capeLoc);
                if (file.exists()) {
                    Cape.setPersonalCape(file);
                    JexPersonalizationScreen.setCape = capeLoc;
                }
                Hat.setHat(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""), hat);
                JexPersonalizationScreen.setHat = hat;
            }
            if (object.get("altening-api-key") != null) {
                String apiKey = object.get("altening-api-key").getAsString();
                TheAlteningHelper.INSTANCE.setApiKey(apiKey);
            }
        } catch (Exception e) {

        }
    }

}
