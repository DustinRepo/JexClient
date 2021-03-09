package me.dustin.jex.helper.network;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public enum MCAPIHelper {
    INSTANCE;

    private final String NAME_API_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private final String UUID_API_URL = "https://api.mojang.com/user/profiles/%s/names";
    private final String PROFILE_REQUEST_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
    private final String STATUS_URL = "https://status.mojang.com/check";

    private static final Identifier STEVE_SKIN = new Identifier("textures/entity/steve.png");

    private HashMap<UUID, String> uuidMap = Maps.newHashMap();
    private HashMap<UUID, Identifier> playerSkins = Maps.newHashMap();
    private HashMap<String, UUID> nameMap = Maps.newHashMap();
    private HashMap<APIServer, APIStatus> serverStatusMap = Maps.newHashMap();
    private ArrayList<String> avatarsRequested = new ArrayList<>();
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
            return null;
        }
    }

    public void downloadPlayerSkin(UUID uuid) {
        if (uuid == null || avatarsRequested.contains(uuid.toString().replace("-", "")))
            return;
        new Thread(() -> {
            try {
                //going to explain what happens so I don't forget
                //request their minecraft profile, all so we can get a base64 encoded string that contains ANOTHER json that then has the skin URL
                String profileResponse = WebHelper.INSTANCE.readURL(new URL(String.format(PROFILE_REQUEST_URL, uuid.toString().replace("-", ""))));

                JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(profileResponse, JsonObject.class);
                //Get the properties array which has what we need
                JsonArray array = object.getAsJsonArray("properties");
                JsonObject property = array.get(0).getAsJsonObject();
                //value is what we grab but it's encoded so we have to decode it
                String base64String = property.get("value").getAsString();
                byte[] bs = Base64.decodeBase64(base64String);
                //Convert the response to json and pull the skin url from there
                String secondResponse = new String(bs, StandardCharsets.UTF_8);
                JsonObject finalResponseObject = JsonHelper.INSTANCE.prettyGson.fromJson(secondResponse, JsonObject.class);
                JsonObject texturesObject = finalResponseObject.getAsJsonObject("textures");
                JsonObject skinObj = texturesObject.getAsJsonObject("SKIN");
                String skinURL = skinObj.get("url").getAsString();
                BufferedImage image = ImageIO.read(new URL(skinURL));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", bos);

                ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());

                NativeImage nativeImage = NativeImage.read(bais);
                Identifier id = new Identifier("jex", "skins/" + uuid.toString().replace("-", ""));
                applyTexture(id, nativeImage);
                avatarsRequested.add(uuid.toString().replace("-", ""));
                playerSkins.put(uuid, id);
            } catch (Exception e) {
                e.printStackTrace();
                avatarsRequested.add(uuid.toString().replace("-", ""));
            }
        }).start();
    }

    public Identifier getPlayerSkin(UUID uuid) {
        if (playerSkins.containsKey(uuid)) {
            return playerSkins.get(uuid);
        } else {
            downloadPlayerSkin(uuid);
        }
        return STEVE_SKIN;
    }

    private static void applyTexture(Identifier identifier, NativeImage nativeImage) {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(nativeImage)));
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
