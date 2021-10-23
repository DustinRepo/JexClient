package me.dustin.jex.helper.network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public enum MCAPIHelper {
    INSTANCE;

    private final String NAME_API_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private final String UUID_API_URL = "https://api.mojang.com/user/profiles/%s/names";
    private final String CHANGE_SKIN_URL = "https://api.minecraftservices.com/minecraft/profile/skins";
    private final String CHECK_MIGRATION_STATUS_URL = "https://api.minecraftservices.com/rollout/v1/msamigration";

    private static final Identifier STEVE_SKIN = new Identifier("textures/entity/steve.png");

    private final HashMap<UUID, String> uuidMap = Maps.newHashMap();
    private final HashMap<UUID, Identifier> playerSkins = Maps.newHashMap();
    private final HashMap<String, UUID> nameMap = Maps.newHashMap();
    private final ArrayList<String> avatarsRequested = new ArrayList<>();

    public boolean setPlayerSkin(String skinURL, SkinVariant skinVariant) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + Wrapper.INSTANCE.getMinecraft().getSession().getAccessToken());
        headers.put("Content-Type", "application/json");
        Map<String, String> jsonData = new HashMap<>();
        jsonData.put("variant", skinVariant.name().toLowerCase());
        jsonData.put("url", skinURL);
        String response = WebHelper.INSTANCE.sendPOST(CHANGE_SKIN_URL, JsonHelper.INSTANCE.gson.toJson(jsonData), headers);
        if (response != null && !response.isEmpty()) {
            JsonArray skins = JsonHelper.INSTANCE.prettyGson.fromJson(response, JsonObject.class).getAsJsonArray("skins");
            if (skins != null) {
                MinecraftClient.getInstance().getSkinProvider().loadSkin(Wrapper.INSTANCE.getMinecraft().getSession().getProfile(), (type, identifier, minecraftProfileTexture) -> {
                    if (type == MinecraftProfileTexture.Type.SKIN) {
                        UUID uuid = Wrapper.INSTANCE.getMinecraft().getSession().getProfile().getId();
                        if (playerSkins.containsKey(uuid))
                            playerSkins.replace(uuid, identifier);
                        else
                            playerSkins.put(uuid, identifier);
                    }

                }, true);
                return true;
            }
        }
        return false;
    }

    public boolean canMigrateAccount() {
        String accessToken = Wrapper.INSTANCE.getMinecraft().getSession().getAccessToken();
        if (accessToken == null || accessToken.isEmpty() || accessToken.equalsIgnoreCase("fakeToken"))
            return false;
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        String response = WebHelper.INSTANCE.readURL(CHECK_MIGRATION_STATUS_URL, headers);
        try {
            JsonObject jsonObject = JsonHelper.INSTANCE.prettyGson.fromJson(response, JsonObject.class);
            String feature = jsonObject.get("feature").getAsString();
            if (feature.equalsIgnoreCase("msamigration")) {
                return jsonObject.get("rollout").getAsBoolean();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getNameFromUUID(UUID uuid) {
        if (uuid == null)
            return "UUID null";
        if (uuidMap.containsKey(uuid))
            return uuidMap.get(uuid);
        String result = null;
        try {
            result = WebHelper.INSTANCE.readURL(new URL(String.format(UUID_API_URL, uuid.toString().replace("-", ""))));
        } catch (IOException e) {
        }
        if (result == null)
            return "Player Not found";
        JsonArray nameArray = JsonHelper.INSTANCE.gson.fromJson(result, JsonArray.class);
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
            return null;
        }
    }

    public void downloadPlayerSkin(UUID uuid) {
        if (uuid == null || avatarsRequested.contains(uuid.toString().replace("-", "")))
            return;
        GameProfile gameProfile = new GameProfile(uuid, "skindl");//name doesn't matter because the url uses the uuid
        avatarsRequested.add(uuid.toString().replace("-", ""));
        //using the handy dandy method Minecraft uses because it actually lets you do something with it rather than just automatically storing them
        MinecraftClient.getInstance().getSkinProvider().loadSkin(gameProfile, (type, identifier, minecraftProfileTexture) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                playerSkins.put(uuid, identifier);
            }

        }, true);
    }

    public Identifier getPlayerSkin(UUID uuid) {
        if (playerSkins.containsKey(uuid)) {
            return playerSkins.get(uuid);
        } else {
            downloadPlayerSkin(uuid);
        }
        return STEVE_SKIN;
    }

    public enum SkinVariant {
        CLASSIC, SLIM
    }
}
