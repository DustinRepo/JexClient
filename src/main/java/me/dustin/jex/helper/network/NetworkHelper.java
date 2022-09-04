package me.dustin.jex.helper.network;

import com.google.gson.JsonObject;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.login.thealtening.TheAlteningHelper;
import me.dustin.jex.load.impl.IMinecraft;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.network.Packet;
import net.minecraft.text.Text;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum NetworkHelper {
    INSTANCE;

    private YggdrasilMinecraftSessionService storedSessionService;
    private Session storedSession;

    public void sendPacket(Packet<?> packet) {
        try {
            if (Wrapper.INSTANCE.getLocalPlayer() != null)
                Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacketDirect(Packet<?> packet) {
        Wrapper.INSTANCE.getLocalPlayer().networkHandler.getConnection().send(packet);
    }

    public void setSessionService(SessionService sessionService) {
        switch (sessionService) {
            case MOJANG -> {
                if (storedSessionService == null)
                    return;
                Wrapper.INSTANCE.getIMinecraft().setSessionService(storedSessionService);
                storedSessionService = null;
            }
            case THEALTENING -> {
                if (storedSessionService != null)
                    return;
                this.storedSessionService = (YggdrasilMinecraftSessionService)Wrapper.INSTANCE.getMinecraft().getSessionService();
                Wrapper.INSTANCE.getIMinecraft().setSessionService(TheAlteningHelper.INSTANCE.getTheAlteningSessionService());
            }
        }
    }

    public YggdrasilMinecraftSessionService getStoredSessionService() {
        return storedSessionService;
    }

    public Session getStoredSession() {
        return storedSession;
    }

    public void setStoredSession(Session storedSession) {
        this.storedSession = storedSession;
    }

    public void disconnect(String reason, String message) {
        boolean bl = Wrapper.INSTANCE.getMinecraft().isInSingleplayer();
        boolean bl2 = Wrapper.INSTANCE.getMinecraft().isConnectedToRealms();
        Wrapper.INSTANCE.getWorld().disconnect();
        if (bl) {
            Wrapper.INSTANCE.getMinecraft().disconnect(new DisconnectedScreen(new TitleScreen(), Text.of("Disconnect"), Text.translatable("menu.savingLevel")));
        } else {
            Wrapper.INSTANCE.getMinecraft().disconnect();
        }

        TitleScreen titleScreen = new TitleScreen();
        if (bl) {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(titleScreen, Text.of(reason), Text.of(message)));
        } else if (bl2) {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(new RealmsMainScreen(titleScreen), Text.of(reason), Text.of(message)));
        } else {
            Wrapper.INSTANCE.getMinecraft().setScreen(new DisconnectedScreen(new MultiplayerScreen(titleScreen), Text.of(reason), Text.of(message)));
        }

    }

    public void updateKeys(Session session) {
        IMinecraft iMinecraft = (IMinecraft)Wrapper.INSTANCE.getMinecraft();
        writeKeyPair(getKeyPairResponse(session.getAccessToken()), uuidFromString(session.getUuid()).toString());
        try {
            iMinecraft.setProfileKeys(new ProfileKeys(iMinecraft.getAuthenticationService().createUserApiService(session.getAccessToken()), session.getProfile().getId(), Wrapper.INSTANCE.getMinecraft().runDirectory.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dropKeys() {
        IMinecraft iMinecraft = (IMinecraft)Wrapper.INSTANCE.getMinecraft();
        iMinecraft.setProfileKeys(new ProfileKeys(UserApiService.OFFLINE, UUID.randomUUID(), Wrapper.INSTANCE.getMinecraft().runDirectory.toPath()));
    }

    private void writeKeyPair(JsonObject keyPairResponse, String uuid) {
        File keysDir = new File(FabricLoaderImpl.INSTANCE.getGameDir().toFile(), "profilekeys");
        if (!keysDir.exists())
            keysDir.mkdirs();
        File keyFile = new File(keysDir, uuid + ".json");
        JsonObject newObject = new JsonObject();
        newObject.addProperty("private_key", keyPairResponse.get("keyPair").getAsJsonObject().get("privateKey").getAsString());
        JsonObject public_key = new JsonObject();
        public_key.addProperty("expires_at", keyPairResponse.get("expiresAt").getAsString());
        public_key.addProperty("key", keyPairResponse.get("keyPair").getAsJsonObject().get("publicKey").getAsString());
        public_key.addProperty("signature", keyPairResponse.get("publicKeySignature").getAsString());
        newObject.add("public_key", public_key);
        newObject.addProperty("refreshed_after", keyPairResponse.get("refreshedAfter").getAsString());
        FileHelper.INSTANCE.writeFile(keyFile, JsonHelper.INSTANCE.gson.toJson(newObject));
    }

    private JsonObject getKeyPairResponse(String accessToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Content-Length", "0");
        headers.put("Authorization", "Bearer " + accessToken);
        WebHelper.HttpResponse httpResponse = WebHelper.INSTANCE.httpRequest("https://api.minecraftservices.com/player/certificates", null, headers, "POST");
        return JsonHelper.INSTANCE.prettyGson.fromJson(httpResponse.data(), JsonObject.class);
    }

    public UUID uuidFromString(String uuid) {
        return UUID.fromString(uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
    }

    public enum SessionService {
        MOJANG, THEALTENING;
    }
}
