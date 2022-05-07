package me.dustin.jex.helper.network.login.thealtening;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.*;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;

import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class TheAlteningSessionService extends HttpMinecraftSessionService {

    private final String SESSION_URL = "http://sessionserver.thealtening.com";
    private final String JOIN_URL = SESSION_URL + "/session/minecraft/join";
    private final String CHECK_URL = SESSION_URL + "/session/minecraft/hasJoined";

    public TheAlteningSessionService() {
        super(null);
    }

    @Override
    public void joinServer(GameProfile profile, String authenticationToken, String serverId){
        JsonObject request = new JsonObject();
        request.addProperty("accessToken", authenticationToken);
        request.addProperty("selectedProfile", profile.getId().toString());
        request.addProperty("serverId", serverId);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        WebHelper.INSTANCE.httpRequest(JOIN_URL, JsonHelper.INSTANCE.gson.toJson(request), header, "GET");
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) {
        final Map<String, Object> arguments = new HashMap<>();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);

        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }
        String httpResponse = WebHelper.INSTANCE.httpRequest(CHECK_URL, arguments, null, "GET").data();
        final HasJoinedMinecraftServerResponse response = JsonHelper.INSTANCE.gson.fromJson(httpResponse, HasJoinedMinecraftServerResponse.class);

        if (response != null && response.getId() != null) {
            final GameProfile result = new GameProfile(response.getId(), user.getName());
            if (response.getProperties() != null) {
                result.getProperties().putAll(response.getProperties());
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        return NetworkHelper.INSTANCE.getStoredSessionService().getTextures(profile, requireSecure);
    }

    @Override
    public GameProfile fillProfileProperties(final GameProfile profile, final boolean requireSecure) {
        //This part uses the base url but just returning the original method doesn't seem to change anything
        return NetworkHelper.INSTANCE.getStoredSessionService().fillProfileProperties(profile, requireSecure);
    }

    @Override
    public String getSecurePropertyValue(Property property) throws InsecurePublicKeyException {
        return NetworkHelper.INSTANCE.getStoredSessionService().getSecurePropertyValue(property);
    }

}
