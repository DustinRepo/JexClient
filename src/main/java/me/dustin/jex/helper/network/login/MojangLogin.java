package me.dustin.jex.helper.network.login;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.files.AltFile;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;
import me.dustin.jex.load.impl.IMinecraft;
import net.minecraft.client.util.Session;

public class MojangLogin {

    private static final String AUTHENTICATE_URL = "https://authserver.mojang.com/authenticate";
    private String email, password;
    private boolean cracked;
    private Consumer<Session> sessionConsumer;

    public MojangLogin(String email, String password, Consumer<Session> sessionConsumer) {
        this.email = email;
        this.password = password;
        this.cracked = !email.contains("@");
        this.sessionConsumer = sessionConsumer;
    }

    public MojangLogin(MinecraftAccount.MojangAccount mojangAccount, Consumer<Session> sessionConsumer) {
        this.email = mojangAccount.getEmail();
        this.password = mojangAccount.getPassword();
        this.cracked = mojangAccount.isCracked();
        this.sessionConsumer = sessionConsumer;
    }

    public static Session login(String email, String password) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("agent", "Minecraft");
        jsonObject.addProperty("username", email);
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("requestUser", true);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        String resp = WebHelper.INSTANCE.sendPOST(AUTHENTICATE_URL, jsonObject.toString(), header);

        if (resp != null && !resp.isEmpty()) {
            JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonObject.class);
            JsonObject selectedProfile = object.get("selectedProfile").getAsJsonObject();
            String name = selectedProfile.get("name").getAsString();
            String uuid = selectedProfile.get("id").getAsString();
            String accessToken = object.get("accessToken").getAsString();
            return new Session(name, uuid, accessToken, "mojang");
        }
        return null;
    }

    public void login() {
        new Thread(() -> {
            NetworkHelper.INSTANCE.resetSessionService();
            Session session;
            if (!cracked)
                session = login(this.email, this.password);
            else
                session = new Session(email, UUID.randomUUID().toString(), "fakeToken", "legacy");
            sessionConsumer.accept(session);
        }).start();
    }
}
