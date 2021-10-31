package me.dustin.jex.file.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.file.core.ConfigFile;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.file.YamlHelper;
import me.dustin.jex.helper.math.EncryptHelper;
import me.dustin.jex.helper.misc.HWID;
import me.dustin.jex.helper.network.login.minecraft.MinecraftAccountManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@ConfigFile.CFG(fileName = "Accounts.yml", folder = "config", bootRead = false)
public class AltFile extends ConfigFile {

    @Override
    public void write() {
        Map<String, Object> yamlMap = new HashMap<>();

        MinecraftAccountManager.INSTANCE.getAccounts().forEach(minecraftAccount -> {
            Map<String, Object> accountMap = new HashMap<>();
            accountMap.put("email", minecraftAccount.getEmail());
            accountMap.put("password", EncryptHelper.INSTANCE.encrypt(HWID.INSTANCE.getHWID(), minecraftAccount.getPassword()));
            accountMap.put("account-type", minecraftAccount instanceof MinecraftAccount.MicrosoftAccount ? "msa" : "mojang");
            if (minecraftAccount instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                accountMap.put("uuid", microsoftAccount.uuid);
                accountMap.put("accessToken", EncryptHelper.INSTANCE.encrypt(HWID.INSTANCE.getHWID(), microsoftAccount.accessToken));
                accountMap.put("refreshToken", EncryptHelper.INSTANCE.encrypt(HWID.INSTANCE.getHWID(), microsoftAccount.refreshToken));
            } else if (minecraftAccount instanceof MinecraftAccount.MojangAccount mojangAccount) {
                accountMap.put("cracked", mojangAccount.isCracked());
            }
            yamlMap.put(minecraftAccount.getUsername(), accountMap);
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
            Map<String, Object> altData = (Map<String, Object>) o;
            String accountType = String.valueOf(altData.get("account-type"));
            String email = String.valueOf(altData.get("email"));
            String password = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), String.valueOf(altData.get("password")));
            if ("msa".equalsIgnoreCase(accountType)) {
                String accessToken = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), String.valueOf(altData.get("accessToken")));
                String refreshToken = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), String.valueOf(altData.get("refreshToken")));
                String uuid = String.valueOf(altData.get("uuid"));
                MinecraftAccount.MicrosoftAccount microsoftAccount = new MinecraftAccount.MicrosoftAccount(s, email, password, accessToken, refreshToken, uuid);
                MinecraftAccountManager.INSTANCE.getAccounts().add(microsoftAccount);
            } else if ("mojang".equalsIgnoreCase(accountType)){
                boolean cracked = (boolean)altData.get("cracked");
                MinecraftAccount.MojangAccount mojangAccount;
                if (cracked)
                    mojangAccount = new MinecraftAccount.MojangAccount(s);
                else
                    mojangAccount = new MinecraftAccount.MojangAccount(s, email, password);
                MinecraftAccountManager.INSTANCE.getAccounts().add(mojangAccount);
            }
        });
    }

    public void exportFile() {
        File file = new File("Accounts-Unencrypted.yml");
        Map<String, Object> yamlMap = new HashMap<>();

        MinecraftAccountManager.INSTANCE.getAccounts().forEach(minecraftAccount -> {
            Map<String, Object> accountMap = new HashMap<>();
            accountMap.put("email", minecraftAccount.getEmail());
            accountMap.put("password", minecraftAccount.getPassword());
            accountMap.put("account-type", minecraftAccount instanceof MinecraftAccount.MicrosoftAccount ? "msa" : "mojang");
            if (minecraftAccount instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                accountMap.put("uuid", microsoftAccount.uuid);
                accountMap.put("accessToken", microsoftAccount.accessToken);
                accountMap.put("refreshToken", microsoftAccount.refreshToken);
            } else if (minecraftAccount instanceof MinecraftAccount.MojangAccount mojangAccount) {
                accountMap.put("cracked", mojangAccount.isCracked());
            }
            yamlMap.put(minecraftAccount.getUsername(), accountMap);
        });

        YamlHelper.INSTANCE.writeFile(yamlMap, file);

    }

    public void importFile() {
        File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Accounts-Unencrypted.yml");
        Map<String, Object> parsedYaml = YamlHelper.INSTANCE.readFile(file);

        parsedYaml.forEach((s, o) -> {
            Map<String, Object> altData = (Map<String, Object>) o;
            String accountType = (String)altData.get("account-type");
            String email = (String)altData.get("email");
            String password = (String)altData.get("password");
            if ("msa".equalsIgnoreCase(accountType)) {
                String accessToken = (String)altData.get("accessToken");
                String refreshToken = (String)altData.get("refreshToken");
                String uuid = (String)altData.get("uuid");
                MinecraftAccount.MicrosoftAccount microsoftAccount = new MinecraftAccount.MicrosoftAccount(s, email, password, accessToken, refreshToken, uuid);
                MinecraftAccountManager.INSTANCE.getAccounts().add(microsoftAccount);
            } else if ("mojang".equalsIgnoreCase(accountType)){
                boolean cracked = (boolean)altData.get("cracked");
                MinecraftAccount.MojangAccount mojangAccount;
                if (cracked)
                    mojangAccount = new MinecraftAccount.MojangAccount(s);
                else
                    mojangAccount = new MinecraftAccount.MojangAccount(s, email, password);
                MinecraftAccountManager.INSTANCE.getAccounts().add(mojangAccount);
            }
        });
    }

    public void convertJson() {
        try {
            File file = new File(ModFileHelper.INSTANCE.getJexDirectory(), "Accounts.json");
            if (!file.exists())
                return;
            StringBuffer stringBuffer = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
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
                if (object.get("email") == null)
                    continue;
                String email = object.get("email").getAsString();
                String password = EncryptHelper.INSTANCE.decrypt(HWID.INSTANCE.getHWID(), object.get("password").getAsString());
                String name = object.get("name").getAsString();
                if (!microsoft) {
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
                    MinecraftAccount.MicrosoftAccount account = new MinecraftAccount.MicrosoftAccount(name, email, password, accessToken, refreshToken, uuid);
                    account.lastUsed = lastUsed;
                    account.loginCount = loginCount;
                    MinecraftAccountManager.INSTANCE.getAccounts().add(account);
                }
            }
            file.delete();
            write();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}