package me.dustin.jex.helper.file.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FriendFile {

    private static String fileName = "Friends.json";

    public static void write() {

        JsonArray jsonArray = new JsonArray();
        for (FriendHelper.Friend friend : FriendHelper.INSTANCE.getFriendsList()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", friend.name());
            jsonObject.addProperty("nickname", friend.alias());
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
                String name = object.get("name").getAsString();
                String nickname = object.get("nickname").getAsString();
                FriendHelper.INSTANCE.addFriend(name, nickname);
            }
        } catch (Exception e) {

        }
    }

}
