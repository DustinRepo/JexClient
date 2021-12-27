package me.dustin.jex.helper.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public enum MCAPIHelper {
    INSTANCE;

    private final String NAME_API_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private final String UUID_API_URL = "https://api.mojang.com/user/profiles/%s/names";
    private final String CHANGE_SKIN_URL = "https://api.minecraftservices.com/minecraft/profile/skins";
    private final String CHECK_MIGRATION_STATUS_URL = "https://api.minecraftservices.com/rollout/v1/msamigration";
    private final String NAME_AVAILABILITY_URL = "https://api.minecraftservices.com/minecraft/profile/name/%s/available";
    private final String NAME_CHANGE_URL = "https://api.minecraftservices.com/minecraft/profile/name/%s";
    private final String NAME_CHANGE_INFO_URL = "https://api.minecraftservices.com/minecraft/profile/namechange";
    private final String SECURITY_QUESTIONS_URL = "https://api.mojang.com/user/security/challenges";
    private final String SECURITY_LOCATION_URL = "https://api.mojang.com/user/security/location";
    private final String STATISTICS_URL = "https://api.mojang.com/orders/statistics";

    private static final Identifier STEVE_SKIN = new Identifier("textures/entity/steve.png");

    private final HashMap<UUID, String> uuidMap = Maps.newHashMap();
    private final HashMap<UUID, Identifier> playerSkins = Maps.newHashMap();
    private final HashMap<String, UUID> nameMap = Maps.newHashMap();
    private final ArrayList<String> avatarsRequested = new ArrayList<>();

    public boolean setPlayerSkin(String skinURL, SkinVariant skinVariant) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getAccessToken());
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

    public boolean needsSecurityQuestions() {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(SECURITY_LOCATION_URL);
            genAuthHeader().forEach(get::setHeader);
            CloseableHttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 403)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<Integer, String> getSecurityQuestions() {
        Map<Integer, String> securityQuestions = new HashMap<>();
        try {
            String resp = WebHelper.INSTANCE.readURL(SECURITY_QUESTIONS_URL, genAuthHeader());
            if (resp != null && !resp.isEmpty()) {
                JsonArray jsonArray = JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonArray.class);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject object = jsonArray.get(i).getAsJsonObject();
                    JsonObject answerObj = object.get("answer").getAsJsonObject();
                    int answerId = answerObj.get("id").getAsInt();
                    JsonObject questionObj = object.get("question").getAsJsonObject();
                    String question = questionObj.get("question").getAsString();
                    securityQuestions.put(answerId, question);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return securityQuestions;
    }

    public String sendSecurityAnswers(Map<Integer, String> answers) {
        Map<String, String> headers = genAuthHeader();
        headers.put("Content-Type", "application/json");
        JsonArray jsonArray = new JsonArray();
        answers.forEach((integer, s) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", integer);
            jsonObject.addProperty("answer", s);
            jsonArray.add(jsonObject);
        });
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(SECURITY_LOCATION_URL);
            StringEntity postingString = new StringEntity(jsonArray.toString());
            post.setEntity(postingString);
            headers.forEach(post::setHeader);
            CloseableHttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204)
                return "success";
            else {
                BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder buffer = new StringBuilder();
                for (String line; (line = input.readLine()) != null; ) {
                    buffer.append(line);
                    buffer.append("\n");
                }
                input.close();
                String resp = buffer.toString();
                JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonObject.class);
                return object.get("errorMessage").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error";
    }

    public boolean isNameAvailable(String name) {
        String resp = WebHelper.INSTANCE.readURL(String.format(NAME_AVAILABILITY_URL, name), genAuthHeader());
        if (resp == null || resp.isEmpty())
            return false;
        JsonObject jsonObject = JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonObject.class);
        return jsonObject.get("status").getAsString().equalsIgnoreCase("AVAILABLE");//either DUPLICATE or AVAILABLE
    }

    public Map<String, String> genAuthHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getAccessToken());
        return headers;
    }

    public boolean canChangeName() {
        String resp = WebHelper.INSTANCE.readURL(NAME_CHANGE_INFO_URL, genAuthHeader());
        if (resp == null || resp.isEmpty())
            return false;
        JsonObject jsonObject = JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonObject.class);
        return jsonObject.get("nameChangeAllowed").getAsBoolean();
    }

    public boolean setName(String name) {
        if (!canChangeName())
            return false;
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPut put = new HttpPut(String.format(NAME_CHANGE_URL, name));
            genAuthHeader().forEach(put::setHeader);
            CloseableHttpResponse response = httpClient.execute(put);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder buffer = new StringBuilder();
                for (String line; (line = input.readLine()) != null; ) {
                    buffer.append(line);
                    buffer.append("\n");
                }
                input.close();
                String json = buffer.toString();
                JsonObject jsonObject = JsonHelper.INSTANCE.prettyGson.fromJson(json, JsonObject.class);
                if (jsonObject.get("name") != null) {
                    if (jsonObject.get("name").getAsString().equalsIgnoreCase(name))
                        return true;
                }
            } else {
                BufferedReader input = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder buffer = new StringBuilder();
                for (String line; (line = input.readLine()) != null; ) {
                    buffer.append(line);
                    buffer.append("\n");
                }
                input.close();
                String json = buffer.toString();
                JexClient.INSTANCE.getLogger().info(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean canMigrateAccount() {
        String accessToken = getAccessToken();
        if (accessToken == null || accessToken.isEmpty() || accessToken.equalsIgnoreCase("fakeToken"))
            return false;
        String response = WebHelper.INSTANCE.readURL(CHECK_MIGRATION_STATUS_URL, genAuthHeader());
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

    public String getAccessToken() {
        return Wrapper.INSTANCE.getMinecraft().getSession().getAccessToken();
    }

    public JsonObject getSalesData() {
        JsonObject object = new JsonObject();
        JsonArray metricKeys = new JsonArray();
        metricKeys.add("item_sold_minecraft");
        metricKeys.add("prepaid_card_redeemed_minecraft");
        object.add("metricKeys", metricKeys);
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        String resp = WebHelper.INSTANCE.sendPOST(STATISTICS_URL, object.toString(), headers);
        if (resp != null && !resp.isEmpty())
            return JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonObject.class);
        return null;
    }

    public Map<String, Long> getNameHistory(UUID uuid) {
        Map<String, Long> names = new HashMap<>();
        try {
            String result = WebHelper.INSTANCE.readURL(new URL(String.format(UUID_API_URL, uuid.toString().replace("-", ""))));
            JsonArray nameArray = JsonHelper.INSTANCE.gson.fromJson(result, JsonArray.class);
            for (int i = 0; i < nameArray.size(); i++) {
                JsonObject object = nameArray.get(i).getAsJsonObject();
                String name = object.get("name").getAsString();
                long changedToAt = -1;
                if (object.get("changedToAt") != null) {
                    changedToAt = object.get("changedToAt").getAsLong();
                }
                names.put(name, changedToAt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return names;
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
        if (name.startsWith("Imported"))
            return null;
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
