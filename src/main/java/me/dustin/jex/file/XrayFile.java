package me.dustin.jex.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.feature.impl.world.Xray;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class XrayFile {

    private static String fileName = "Xray.json";

    public static void write() {

        JsonArray jsonArray = new JsonArray();
        for (Block block : Xray.blockList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("block", Registry.BLOCK.getId(block).toString());
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
        try {
            Xray.blockList.clear();
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(ModFileHelper.INSTANCE.getJexDirectory(), fileName).getPath()), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String blockName = object.get("block").getAsString();
                Block block = Registry.BLOCK.get(new Identifier(blockName));
                Xray.blockList.add(block);
            }
        } catch (Exception e) {

        }
    }

}
