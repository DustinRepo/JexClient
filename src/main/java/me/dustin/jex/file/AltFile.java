package me.dustin.jex.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.account.MinecraftAccountManager;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.math.EncryptHelper;
import me.dustin.jex.helper.misc.HWID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AltFile {

    private static String fileName = "Accounts.json";

    public static void write() {

        JsonArray jsonArray = new JsonArray();
        for (MinecraftAccount alt : MinecraftAccountManager.INSTANCE.getAccounts()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", alt.getUsername());
            jsonObject.addProperty("email", alt.getEmail());
            jsonObject.addProperty("password", EncryptHelper.INSTANCE.encrypt(HWID.INSTANCE.getHWID(), alt.getPassword()));
            jsonObject.addProperty("cracked", alt.isCracked());
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

    public static void exportFile() {
        String fileName = "Accounts-Unencrypted.json";
        JsonArray jsonArray = new JsonArray();
        for (MinecraftAccount alt : MinecraftAccountManager.INSTANCE.getAccounts()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", alt.getUsername());
            jsonObject.addProperty("email", alt.getEmail());
            jsonObject.addProperty("password", alt.getPassword());
            jsonObject.addProperty("cracked", alt.isCracked());
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

    public static void importFile() {
        String fileName = "Accounts-Unencrypted.json";
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
                String name = object.get("name").getAsString();
                String email = object.get("email").getAsString();
                String password = object.get("password").getAsString();
                boolean cracked = object.get("cracked").getAsBoolean();
                MinecraftAccount account = new MinecraftAccount(name, email, password);
                account.setCracked(cracked);
                MinecraftAccountManager.INSTANCE.getAccounts().add(account);
            }
        } catch (Exception e) {

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
                String name = object.get("name").getAsString();
                String email = object.get("email").getAsString();
                String password = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), object.get("password").getAsString());
                boolean cracked = object.get("cracked").getAsBoolean();
                MinecraftAccount account = new MinecraftAccount(name, email, password);
                account.setCracked(cracked);
                MinecraftAccountManager.INSTANCE.getAccounts().add(account);
            }
        } catch (Exception e) {

        }
    }

}
