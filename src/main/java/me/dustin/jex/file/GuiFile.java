package me.dustin.jex.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.gui.click.ClickGui;
import me.dustin.jex.gui.click.impl.Window;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GuiFile {

    private static String fileName = "Gui.json";

    public static void write() {
        JsonArray jsonArray = new JsonArray();
        for (Window window : ClickGui.windows) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("Name", window.getName());
            jsonObject.addProperty("X", window.getX());
            jsonObject.addProperty("Y", window.getY());
            jsonObject.addProperty("Open", window.isOpen());
            jsonObject.addProperty("Pinned", window.isPinned());
            jsonArray.add(jsonObject);

            ArrayList<String> stringList = new ArrayList<>();
            for (String s : JsonHelper.INSTANCE.prettyGson.toJson(jsonArray).split("\n")) {
                stringList.add(s);
            }

            try {
                FileHelper.INSTANCE.writeFile(ModFileHelper.INSTANCE.getJexDirectory(), fileName, stringList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void read() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), fileName);
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
    }

}
