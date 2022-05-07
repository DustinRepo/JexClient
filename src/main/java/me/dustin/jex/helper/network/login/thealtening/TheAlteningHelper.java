package me.dustin.jex.helper.network.login.thealtening;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.platform.NativeImage;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.User;
import net.minecraft.resources.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public enum TheAlteningHelper {
    INSTANCE;

    private final String GENERATE_URL = "http://api.thealtening.com/v2/generate?key=%s";
    private final String LICENSE_URL = "http://api.thealtening.com/v2/license?key=%s";
    private final String SKIN_URL = "https://cdn.thealtening.com/skins/body/%s.png";
    private final String FAVORITE_ACC_URL = "http://api.thealtening.com/v2/favorite?key=%s&token=%s";
    private final String PRIVATE_ACC_URL = "http://api.thealtening.com/v2/private?key=%s&token=%s";
    private final String FAVORITES_URL = "http://api.thealtening.com/v2/favorites?key=%s";
    private final String PRIVATES_URL = "http://api.thealtening.com/v2/privates?key=%s";
    private final String AUTH_URL = "http://authserver.thealtening.com/authenticate";

    private YggdrasilAuthenticationService THE_ALTENING_AUTH;
    private TheAlteningSessionService THE_ALTENING_SESSION_SERVICE;

    private String apiKey = "";
    private TheAlteningLicense license;
    private final ArrayList<TheAlteningAccount> avatarsRequested = new ArrayList<>();
    private final HashMap<TheAlteningAccount, ResourceLocation> skins = new HashMap<>();
    private final ResourceLocation STEVE_SKIN = new ResourceLocation("textures/entity/steve.png");

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public TheAlteningAccount generateAccount() {
        String GENERATE_URL = String.format(this.GENERATE_URL, this.getApiKey());
        String resp = WebHelper.INSTANCE.httpRequest(GENERATE_URL, null, null, "GET").data();
        return JsonHelper.INSTANCE.gson.fromJson(resp, TheAlteningAccount.class);
    }

    public void fetchLicense() {
        String LICENSE_URL = String.format(this.LICENSE_URL, this.getApiKey());
        String resp = WebHelper.INSTANCE.httpRequest(LICENSE_URL, null, null, "GET").data();
        this.license = JsonHelper.INSTANCE.gson.fromJson(resp, TheAlteningLicense.class);
    }

    public ArrayList<TheAlteningAccount> getFavorites() {
        ArrayList<TheAlteningAccount> accounts = new ArrayList<>();
        String FAVORITES_URL = String.format(this.FAVORITES_URL, this.getApiKey());
        String resp = WebHelper.INSTANCE.httpRequest(FAVORITES_URL, null, null, "GET").data();
        JsonArray array = JsonHelper.INSTANCE.gson.fromJson(resp, JsonArray.class);
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                accounts.add(JsonHelper.INSTANCE.gson.fromJson(array.get(i).getAsJsonObject().toString(), TheAlteningAccount.class));
            }
        }
        return accounts;
    }

    public ArrayList<TheAlteningAccount> getPrivates() {
        ArrayList<TheAlteningAccount> accounts = new ArrayList<>();
        String PRIVATES_URL = String.format(this.PRIVATES_URL, this.getApiKey());
        String resp = WebHelper.INSTANCE.httpRequest(PRIVATES_URL, null, null, "GET").data();
        JsonArray array = JsonHelper.INSTANCE.gson.fromJson(resp, JsonArray.class);
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                accounts.add(JsonHelper.INSTANCE.gson.fromJson(array.get(i).getAsJsonObject().toString(), TheAlteningAccount.class));
            }
        }
        return accounts;
    }

    public boolean favoriteAcc(TheAlteningAccount theAlteningAccount) {
        String FAVORITE_ACC_URL = String.format(this.FAVORITE_ACC_URL, this.getApiKey(), theAlteningAccount.token);
        String resp = WebHelper.INSTANCE.httpRequest(FAVORITE_ACC_URL, null, null, "GET").data();
        if (resp != null)
            return resp.contains("success") && resp.contains("true");
        return false;
    }

    public boolean privateAcc(TheAlteningAccount theAlteningAccount) {
        String PRIVATE_ACC_URL = String.format(this.PRIVATE_ACC_URL, this.getApiKey(), theAlteningAccount.token);
        String resp = WebHelper.INSTANCE.httpRequest(PRIVATE_ACC_URL, null, null, "GET").data();
        if (resp != null)
            return resp.contains("success") && resp.contains("true");
        return false;
    }

    public void login(TheAlteningAccount theAlteningAccount, Consumer<User> sessionConsumer) {
        login(theAlteningAccount.token, sessionConsumer);
    }

    public void login(String token, Consumer<User> sessionConsumer) {
        new Thread(() -> {
            NetworkHelper.INSTANCE.setSessionService(NetworkHelper.SessionService.THEALTENING);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("agent", "Minecraft");
            jsonObject.addProperty("username", token);
            jsonObject.addProperty("password", "JexClient");
            jsonObject.addProperty("requestUser", true);
            Map<String, String> header = new HashMap<>();
            header.put("Content-Type", "application/json");
            String resp = WebHelper.INSTANCE.httpRequest(AUTH_URL, jsonObject.toString(), header, "POST").data();

            if (resp != null && !resp.isEmpty()) {
                JsonObject object = JsonHelper.INSTANCE.prettyGson.fromJson(resp, JsonObject.class);
                JsonObject selectedProfile = object.get("selectedProfile").getAsJsonObject();
                String name = selectedProfile.get("name").getAsString();
                String uuid = selectedProfile.get("id").getAsString();
                String accessToken = object.get("accessToken").getAsString();
                sessionConsumer.accept(new User(name, uuid, accessToken, Optional.of(""), Optional.of(""), User.Type.MOJANG));
            }
        }).start();
    }

    public ResourceLocation getSkin(TheAlteningAccount account) {
        if (!avatarsRequested.contains(account)) {
            String SKIN_URL = String.format(this.SKIN_URL, account.skin);
            skins.put(account, STEVE_SKIN);
            avatarsRequested.add(account);

            new Thread(() -> {
                try {
                    BufferedImage in = ImageIO.read(new URL(SKIN_URL));
                    BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = newImage.createGraphics();
                    g.drawImage(in, 0, 0, null);
                    g.dispose();
                    NativeImage image = FileHelper.INSTANCE.readTexture(FileHelper.INSTANCE.imageToBase64String(newImage, "png"));

                    NativeImage imgNew = new NativeImage(image.getWidth(), image.getHeight(), true);
                    for (int x = 0; x < image.getWidth(); x++) {
                        for (int y = 0; y < image.getHeight(); y++) {
                            imgNew.setPixelRGBA(x, y, image.getPixelRGBA(x, y));
                        }
                    }

                    image.close();
                    ResourceLocation id = new ResourceLocation("jex", "thealtening/" + account.username.replace("*", "").toLowerCase() + ".png");
                    FileHelper.INSTANCE.applyTexture(id, imgNew);
                    skins.replace(account, id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        return skins.get(account);
    }

    public boolean isConnectedToAltening() {
        return Wrapper.INSTANCE.getMinecraft().getUser().getAccessToken().startsWith("alt_");
    }

    public boolean hasValidLicense() {
        return getLicense() != null && getLicense().hasLicense;
    }

    public TheAlteningLicense getLicense() {
        return this.license;
    }

    public TheAlteningSessionService getTheAlteningSessionService() {
        if (THE_ALTENING_SESSION_SERVICE == null)
            THE_ALTENING_SESSION_SERVICE = new TheAlteningSessionService();
        return THE_ALTENING_SESSION_SERVICE;
    }

    public static class TheAlteningLicense {
        public String username;
        public boolean hasLicense;
        public String licenseType;
        public String expires;
    }

    public static class TheAlteningAccount {
        public String token;
        public String password;
        public String username;
        public boolean limit;
        public String skin;
    }
}
