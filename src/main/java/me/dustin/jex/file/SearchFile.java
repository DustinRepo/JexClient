package me.dustin.jex.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.module.impl.render.Search;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Optional;

public class SearchFile {

    private static String fileName = "Search.json";

    public static void write() {

        JsonArray jsonArray = new JsonArray();
        for (Block block : Search.getBlocks().keySet()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("mod", Registry.BLOCK.getId(block).getNamespace());
            jsonObject.addProperty("blockID", Registry.BLOCK.getId(block).toString());
            jsonObject.addProperty("color", Integer.toHexString(Search.getBlocks().get(block)));
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
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(ModFileHelper.INSTANCE.getJexDirectory(), fileName).getPath()), "UTF8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(String.valueOf(stringBuffer), JsonArray.class);
            in.close();
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                String blockID = object.get("blockID").getAsString();
                Optional<Block> block = Registry.BLOCK.getOrEmpty(new Identifier(blockID));
                int color = Render2DHelper.INSTANCE.hex2Rgb(object.get("color").getAsString()).getRGB();
                Search.getBlocks().put(block.get(), color);
            }
        } catch (Exception e) {

        }
    }

}
