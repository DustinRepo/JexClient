package me.dustin.jex.file.impl;

import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.addon.cape.Cape;
import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.gui.jex.JexPersonalizationScreen;
import me.dustin.jex.gui.minecraft.JexTitleScreen;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.thealtening.TheAlteningHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Settings.yml", folder = "config")
public class ClientSettingsFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        yamlMap.put("prefix", CommandManagerJex.INSTANCE.getPrefix());
        yamlMap.put("main-menu-bg", JexTitleScreen.background);
        yamlMap.put("auto-save", JexClient.INSTANCE.isAutoSaveEnabled());
        yamlMap.put("launch-sound", JexClient.INSTANCE.playSoundOnLaunch());
        yamlMap.put("personal-cape", JexPersonalizationScreen.setCape);
        yamlMap.put("personal-hat", JexPersonalizationScreen.setHat);
        yamlMap.put("altening-api-key", TheAlteningHelper.INSTANCE.getApiKey());

        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        convertJson();
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedYaml == null || parsedYaml.isEmpty())
            return;
        CommandManagerJex.INSTANCE.setPrefix((String)parsedYaml.get("prefix"));
        JexTitleScreen.background = (int)parsedYaml.get("main-menu-bg");
        JexClient.INSTANCE.setAutoSave((boolean)parsedYaml.get("auto-save"));
        JexClient.INSTANCE.setPlaySoundOnLaunch((boolean)parsedYaml.get("launch-sound"));
        JexPersonalizationScreen.setCape = (String)parsedYaml.get("personal-cape");
        JexPersonalizationScreen.setHat = (String)parsedYaml.get("personal-hat");
        TheAlteningHelper.INSTANCE.setApiKey((String)parsedYaml.get("altening-api-key"));

        if (JexPersonalizationScreen.setCape != null) {
            File capeFile = new File(JexPersonalizationScreen.setCape);
            Cape.setPersonalCape(capeFile);
        }
        if (JexPersonalizationScreen.setHat != null && !JexPersonalizationScreen.setHat.equalsIgnoreCase("None")) {
            Hat.setHat(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""), JexPersonalizationScreen.setHat);
        }
    }


    public void convertJson() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Settings.json");
        if (!file.exists())
            return;
        try {
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonObject.class);
            in.close();
            CommandManagerJex.INSTANCE.setPrefix(object.get("prefix").getAsString());
            JexTitleScreen.background = object.get("main-menu-bg").getAsInt();
            JexClient.INSTANCE.setAutoSave(object.get("auto-save").getAsBoolean());
            JexClient.INSTANCE.setPlaySoundOnLaunch(object.get("launch-sound").getAsBoolean());

            if (object.get("personal-cape") != null) {
                String capeLoc = object.get("personal-cape").getAsString();
                String hat = object.get("personal-hat").getAsString();
                File capeFile = new File(capeLoc);
                if (capeFile.exists()) {
                    Cape.setPersonalCape(capeFile);
                    JexPersonalizationScreen.setCape = capeLoc;
                }
                Hat.setHat(Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""), hat);
                JexPersonalizationScreen.setHat = hat;
            }
            if (object.get("altening-api-key") != null) {
                String apiKey = object.get("altening-api-key").getAsString();
                TheAlteningHelper.INSTANCE.setApiKey(apiKey);
            }
            file.delete();
            write();
        } catch (Exception e) {

        }
    }
}
