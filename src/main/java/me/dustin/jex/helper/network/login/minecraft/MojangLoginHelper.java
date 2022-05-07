package me.dustin.jex.helper.network.login.minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.User;

public class MojangLoginHelper {

    private static final String AUTHENTICATE_URL = "https://authserver.mojang.com/authenticate";
    private final String email;
    private final String password;
    private final boolean cracked;
    private final Consumer<User> sessionConsumer;

    public MojangLoginHelper(String email, String password, Consumer<User> sessionConsumer) {
        this.email = email;
        this.password = password;
        this.cracked = !email.contains("@");
        this.sessionConsumer = sessionConsumer;
    }

    public MojangLoginHelper(MinecraftAccount.MojangAccount mojangAccount, Consumer<User> sessionConsumer) {
        this.email = mojangAccount.isCracked() ? mojangAccount.getUsername() : mojangAccount.getEmail();
        this.password = mojangAccount.getPassword();
        this.cracked = mojangAccount.isCracked();
        this.sessionConsumer = sessionConsumer;
    }

    public static User login(String email, String password) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("agent", "Minecraft");
        jsonObject.addProperty("username", email);
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("requestUser", true);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        String resp = WebHelper.INSTANCE.httpRequest(AUTHENTICATE_URL, jsonObject.toString(), header, "POST").data();

        if (resp != null && !resp.isEmpty()) {
            JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonObject.class);
            JsonObject selectedProfile = object.get("selectedProfile").getAsJsonObject();
            String name = selectedProfile.get("name").getAsString();
            String uuid = selectedProfile.get("id").getAsString();
            String accessToken = object.get("accessToken").getAsString();
            return new User(name, uuid, accessToken, Optional.of(""), Optional.of(""), User.Type.MOJANG);
        }
        return null;
    }

    public void login() {
        if (!cracked)
        new Thread(() -> {
            NetworkHelper.INSTANCE.setSessionService(NetworkHelper.SessionService.MOJANG);
            User session = login(this.email, this.password);
            sessionConsumer.accept(session);
        }).start();
        else
            sessionConsumer.accept(new User(email, UUID.randomUUID().toString(), "fakeToken", Optional.of(""), Optional.of(""), User.Type.LEGACY));
    }
}
