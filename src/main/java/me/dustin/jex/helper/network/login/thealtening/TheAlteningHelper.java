package me.dustin.jex.helper.network.login.thealtening;

import com.google.gson.JsonArray;
import com.mojang.authlib.Agent;
import com.mojang.authlib.Environment;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import me.dustin.jex.helper.file.FileHelper;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Session;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final String AUTH_URL = "http://authserver.thealtening.com";
    private final String ACCOUNT_URL = "https://api.mojang.com";
    private final String SESSION_URL = "http://sessionserver.thealtening.com";
    private final String SERVICES_URL = "https://api.minecraftservices.com";

    private YggdrasilAuthenticationService THE_ALTENING_AUTH;
    private TheAlteningSessionService THE_ALTENING_SESSION_SERVICE;

    private String apiKey = "";
    private TheAlteningLicense license;
    private final ArrayList<TheAlteningAccount> avatarsRequested = new ArrayList<>();
    private final HashMap<TheAlteningAccount, Identifier> skins = new HashMap<>();
    private final Identifier STEVE_SKIN = new Identifier("textures/entity/steve.png");

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public TheAlteningAccount generateAccount() {
        String GENERATE_URL = String.format(this.GENERATE_URL, this.getApiKey());
        String resp = WebHelper.INSTANCE.readURL(GENERATE_URL);
        return JsonHelper.INSTANCE.gson.fromJson(resp, TheAlteningAccount.class);
    }

    public void fetchLicense() {
        String LICENSE_URL = String.format(this.LICENSE_URL, this.getApiKey());
        String resp = WebHelper.INSTANCE.readURL(LICENSE_URL);
        this.license = JsonHelper.INSTANCE.gson.fromJson(resp, TheAlteningLicense.class);
    }

    public ArrayList<TheAlteningAccount> getFavorites() {
        ArrayList<TheAlteningAccount> accounts = new ArrayList<>();
        String FAVORITES_URL = String.format(this.FAVORITES_URL, this.getApiKey());
        String resp = WebHelper.INSTANCE.readURL(FAVORITES_URL);
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
        String resp = WebHelper.INSTANCE.readURL(PRIVATES_URL);
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
        String resp = WebHelper.INSTANCE.readURL(FAVORITE_ACC_URL);
        if (resp != null)
            return resp.contains("success") && resp.contains("true");
        return false;
    }

    public boolean privateAcc(TheAlteningAccount theAlteningAccount) {
        String PRIVATE_ACC_URL = String.format(this.PRIVATE_ACC_URL, this.getApiKey(), theAlteningAccount.token);
        String resp = WebHelper.INSTANCE.readURL(PRIVATE_ACC_URL);
        if (resp != null)
            return resp.contains("success") && resp.contains("true");
        return false;
    }

    public void login(TheAlteningAccount theAlteningAccount, Consumer<Session> sessionConsumer) {
        login(theAlteningAccount.token, sessionConsumer);
    }

    public void login(String token, Consumer<Session> sessionConsumer) {
        new Thread(() -> {
            NetworkHelper.INSTANCE.setSessionService(NetworkHelper.SessionService.THEALTENING);
            YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) getTheAlteningAuth().createUserAuthentication(Agent.MINECRAFT);
            auth.setUsername(token);
            auth.setPassword("JexClient");
            try {
                auth.logIn();
                sessionConsumer.accept(new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang"));
                return;
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }

            sessionConsumer.accept(null);
        }).start();
    }

    public Identifier getSkin(TheAlteningAccount account) {
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
                            imgNew.setPixelColor(x, y, image.getPixelColor(x, y));
                        }
                    }

                    image.close();
                    Identifier id = new Identifier("jex", "thealtening/" + account.username.replace("*", "").toLowerCase() + ".png");
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
        return Wrapper.INSTANCE.getMinecraft().getSession().getAccessToken().startsWith("alt_");
    }

    public TheAlteningLicense getLicense() {
        return this.license;
    }

    public YggdrasilAuthenticationService getTheAlteningAuth() {
        if (THE_ALTENING_AUTH == null)
            THE_ALTENING_AUTH = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "", Environment.create(AUTH_URL, ACCOUNT_URL, SESSION_URL, SERVICES_URL, "TheAltening"));
        return THE_ALTENING_AUTH;
    }

    public TheAlteningSessionService getTheAlteningSessionService() {
        if (THE_ALTENING_SESSION_SERVICE == null)
            THE_ALTENING_SESSION_SERVICE = new TheAlteningSessionService(getTheAlteningAuth());
        return THE_ALTENING_SESSION_SERVICE;
    }

    public class TheAlteningLicense {
        public String username;
        public boolean hasLicense;
        public String licenseType;
        public String expires;
    }

    public class TheAlteningAccount {
        public String token;
        public String password;
        public String username;
        public boolean limit;
        public String skin;
    }
}
