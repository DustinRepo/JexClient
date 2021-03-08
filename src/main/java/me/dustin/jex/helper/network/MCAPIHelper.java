package me.dustin.jex.helper.network;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Timer;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public enum MCAPIHelper {
    INSTANCE;

    private final String NAME_API_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private final String UUID_API_URL = "https://api.mojang.com/user/profiles/%s/names";
    private final String STATUS_URL = "https://status.mojang.com/check";

    private HashMap<UUID, String> uuidMap = Maps.newHashMap();
    private HashMap<String, UUID> nameMap = Maps.newHashMap();
    private HashMap<APIServer, APIStatus> serverStatusMap = Maps.newHashMap();
    private Timer timer = new Timer();

    public String getNameFromUUID(UUID uuid) {
        if (uuid == null)
            return "UUID null";
        if (getStatus(APIServer.API_MOJANG) == APIStatus.RED)
            return "API Server down";
        if (uuidMap.containsKey(uuid))
            return uuidMap.get(uuid);
        String result = null;
        try {
            result = WebHelper.INSTANCE.readURL(new URL(String.format(UUID_API_URL, uuid.toString().replace("-", ""))));
        } catch (IOException e) {
        }
        if (result == null)
            return "Player Not found";
        JsonArray nameArray = JsonHelper.INSTANCE.gson.fromJson(result.toString(), JsonArray.class);
        try {
            JsonObject object = nameArray.get(nameArray.size() - 1).getAsJsonObject();

            String name = object.get("name").getAsString();
            uuidMap.putIfAbsent(uuid, name);
            return name;
        } catch (Exception e) {
            return "Error";
        }
    }

    public UUID getUUIDFromName(String name) {
        try {
            if (nameMap.containsKey(name))
                return nameMap.get(name);
            String result = null;
            try {
                result = WebHelper.INSTANCE.readURL(new URL(String.format(NAME_API_URL, name)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result == null)
                return null;
            JsonObject object =  JsonHelper.INSTANCE.gson.fromJson(result, JsonObject.class);
            UUID uuid = UUID.fromString(object.get("id").getAsString().replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            ));
            nameMap.putIfAbsent(name, uuid);
            return uuid;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public APIStatus getStatus(APIServer server) {
        updateStatus();
        if (!serverStatusMap.containsKey(server))
            return APIStatus.RED;
        return serverStatusMap.get(server);
    }

    private void updateStatus() {
        if (!timer.hasPassed(30000))
            return;
        serverStatusMap.clear();
        String result = null;
        try {
            result = WebHelper.INSTANCE.readURL(new URL(STATUS_URL));
        } catch (IOException e) {
        }
        if (result == null)
            return;
        JsonArray nameArray = JsonHelper.INSTANCE.gson.fromJson(result, JsonArray.class);
        JsonObject object = nameArray.get(0).getAsJsonObject();

        for (int i = 0; i < nameArray.size(); i++) {
            JsonObject object1 = nameArray.get(i).getAsJsonObject();
            String serverName = object1.toString().split("\"")[1];
            String response = object1.get(serverName).getAsString();

            serverStatusMap.put(getServer(serverName), APIStatus.valueOf(response.toUpperCase()));
        }
        timer.reset();
    }

    private APIServer getServer(String server) {
        switch (server) {
            case "minecraft.net":
                return APIServer.MINECRAFT_NET;
            case "session.minecraft.net":
                return APIServer.SESSION;
            case "account.mojang.com":
                return APIServer.ACCOUNT;
            case "authserver.mojang.com"://
                return APIServer.AUTHSERVER;
            case "sessionserver.mojang.com"://
                return APIServer.SESSIONSERVER;
            case "api.mojang.com"://
                return APIServer.API_MOJANG;
            case "textures.minecraft.net"://
                return APIServer.TEXTURES;
            case "mojang.com"://
                return APIServer.MOJANG_COM;
        }
        return null;
    }


    public enum APIStatus {
        GREEN, YELLOW, RED
    }

    public enum APIServer {
        MINECRAFT_NET("Minecraft.net"),
        SESSION("session.Minecraft.net"),
        ACCOUNT("account.Mojang.com"),
        AUTHSERVER("authserver.Mojang.com"),
        SESSIONSERVER("sessionserver.Mojang.com"),
        API_MOJANG("api.Mojang.com"),
        TEXTURES("textures.Minecraft.net"),
        MOJANG_COM("Mojang.com");

        private final String name;

        APIServer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
