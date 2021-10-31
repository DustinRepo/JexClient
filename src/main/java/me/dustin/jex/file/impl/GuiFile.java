package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.gui.click.window.ClickGui;
import me.dustin.jex.gui.click.window.impl.Window;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Gui.yml", folder = "config")
public class GuiFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();
        ClickGui.windows.forEach(window -> {
            Map<String, Object> windowData = new HashMap<>();
            windowData.put("X", window.getX());
            windowData.put("Y", window.getY());
            windowData.put("Open", window.isOpen());
            windowData.put("Pinned", window.isPinned());
            yamlMap.put(window.getName(), windowData);
        });
        YamlHelper.INSTANCE.writeFile(yamlMap, getFile());
    }

    @Override
    public void read() {
        convertJson();
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(getFile());
        if (parsedYaml == null || parsedYaml.isEmpty())
            return;
        parsedYaml.forEach((s, o) -> {
            Window window = ClickGui.getWindow(s);
            if (window == null)
                return;
            Map<String, Object> windowData = (Map<String, Object>)o;
            float x = (float)(double)windowData.get("X");//yaml likes to explictly set the float options to double
            float y = (float)(double)windowData.get("Y");
            boolean open = (boolean)windowData.get("Open");
            boolean pinned = (boolean)windowData.get("Pinned");
            float moveX = x - window.getX();
            float moveY = y - window.getY();
            window.setX(x);
            window.setY(y);
            window.getButtons().forEach(button -> {
                window.moveAll(button, moveX, moveY);
            });
            window.setOpen(open);
            window.setPinned(pinned);
        });
    }

    public void convertJson() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Gui.json");
        if (!file.exists())
            return;
        try {
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "UTF8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String name = object.get("Name").getAsString();
                float x = object.get("X").getAsFloat();
                float y = object.get("Y").getAsFloat();
                boolean isOpen = object.get("Open").getAsBoolean();
                boolean isPinned = object.get("Pinned").getAsBoolean();
                Window window = ClickGui.getWindow(name);
                if (window != null) {
                    float moveX = x - window.getX();
                    float moveY = y - window.getY();
                    window.setX(x);
                    window.setY(y);
                    window.getButtons().forEach(button -> {
                        window.moveAll(button, moveX, moveY);
                    });
                    window.setOpen(isOpen);
                    window.setPinned(isPinned);
                }
            }
        } catch (Exception e) {

        }
        file.delete();
        write();
    }

}
