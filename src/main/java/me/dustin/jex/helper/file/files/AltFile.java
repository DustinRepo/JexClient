package me.dustin.jex.helper.file.files;

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
            if (alt instanceof MinecraftAccount.MojangAccount mojangAccount) {
                jsonObject.addProperty("email", mojangAccount.getEmail());
                jsonObject.addProperty("password", EncryptHelper.INSTANCE.encrypt(HWID.INSTANCE.getHWID(), mojangAccount.getPassword()));
                jsonObject.addProperty("cracked", mojangAccount.isCracked());
                jsonObject.addProperty("Microsoft", false);
            } else if (alt instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                jsonObject.addProperty("uuid", microsoftAccount.uuid);
                jsonObject.addProperty("accessToken", EncryptHelper.INSTANCE.encrypt(HWID.INSTANCE.getHWID(), microsoftAccount.accessToken));
                jsonObject.addProperty("refreshToken", EncryptHelper.INSTANCE.encrypt(HWID.INSTANCE.getHWID(), microsoftAccount.refreshToken));
                jsonObject.addProperty("Microsoft", true);
            }
            jsonObject.addProperty("loginCount", alt.loginCount);
            jsonObject.addProperty("lastUsed", alt.lastUsed);


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
            if (alt instanceof MinecraftAccount.MojangAccount mojangAccount) {
                jsonObject.addProperty("email", mojangAccount.getEmail());
                jsonObject.addProperty("password", mojangAccount.getPassword());
                jsonObject.addProperty("cracked", mojangAccount.isCracked());
                jsonObject.addProperty("Microsoft", false);
            } else if (alt instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                jsonObject.addProperty("uuid", microsoftAccount.uuid);
                jsonObject.addProperty("accessToken", microsoftAccount.accessToken);
                jsonObject.addProperty("refreshToken", microsoftAccount.refreshToken);
                jsonObject.addProperty("Microsoft", true);
            }
            jsonObject.addProperty("loginCount", alt.loginCount);
            jsonObject.addProperty("lastUsed", alt.lastUsed);
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
                boolean microsoft = false;
                int loginCount = 0;
                long lastUsed = -1L;
                if (object.get("Microsoft") != null) {
                    microsoft = object.get("Microsoft").getAsBoolean();
                    loginCount = object.get("loginCount").getAsInt();
                    lastUsed = object.get("lastUsed").getAsLong();
                }
                String name = object.get("name").getAsString();
                if (!microsoft) {
                    String email = object.get("email").getAsString();
                    String password = object.get("password").getAsString();
                    boolean cracked = object.get("cracked").getAsBoolean();
                    MinecraftAccount.MojangAccount account = new MinecraftAccount.MojangAccount(name, email, password);
                    account.lastUsed = lastUsed;
                    account.loginCount = loginCount;
                    account.setCracked(cracked);
                    MinecraftAccountManager.INSTANCE.getAccounts().add(account);
                } else {
                    String accessToken = object.get("accessToken").getAsString();
                    String refreshToken = object.get("refreshToken").getAsString();
                    String uuid = object.get("uuid") == null ? "null" : object.get("uuid").getAsString();

                    MinecraftAccount.MicrosoftAccount account = new MinecraftAccount.MicrosoftAccount(name, accessToken, refreshToken, uuid);
                    account.lastUsed = lastUsed;
                    account.loginCount = loginCount;
                    MinecraftAccountManager.INSTANCE.getAccounts().add(account);
                }
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
                boolean microsoft = false;
                int loginCount = 0;
                long lastUsed = 0;
                if (object.get("Microsoft") != null) {
                    microsoft = object.get("Microsoft").getAsBoolean();
                    loginCount = object.get("loginCount").getAsInt();
                    lastUsed = object.get("lastUsed").getAsLong();
                }
                String name = object.get("name").getAsString();
                if (!microsoft) {
                    String email = object.get("email").getAsString();
                    String password = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), object.get("password").getAsString());
                    boolean cracked = object.get("cracked").getAsBoolean();
                    MinecraftAccount.MojangAccount account = new MinecraftAccount.MojangAccount(name, email, password);
                    account.setCracked(cracked);
                    account.lastUsed = lastUsed;
                    account.loginCount = loginCount;
                    MinecraftAccountManager.INSTANCE.getAccounts().add(account);
                } else {
                    String accessToken = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), object.get("accessToken").getAsString());
                    String refreshToken = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), object.get("refreshToken").getAsString());
                    String uuid = object.get("uuid") == null ? "null" : object.get("uuid").getAsString();
                    MinecraftAccount.MicrosoftAccount account = new MinecraftAccount.MicrosoftAccount(name, accessToken, refreshToken, uuid);
                    account.lastUsed = lastUsed;
                    account.loginCount = loginCount;
                    MinecraftAccountManager.INSTANCE.getAccounts().add(account);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
