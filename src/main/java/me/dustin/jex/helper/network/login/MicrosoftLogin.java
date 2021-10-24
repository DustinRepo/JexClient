package me.dustin.jex.helper.network.login;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.gui.account.AccountManagerScreen;
import me.dustin.jex.gui.account.AddAccountScreen;
import me.dustin.jex.gui.account.DirectLoginScreen;
import me.dustin.jex.gui.account.account.MinecraftAccount;
import me.dustin.jex.gui.account.account.MinecraftAccountManager;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.files.AltFile;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.util.Session;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MicrosoftLogin {

    private String email, password, accessToken, refreshToken;
    private boolean saveAccount;

    public MicrosoftLogin(MinecraftAccount.MicrosoftAccount microsoftAccount) {
        this.email = microsoftAccount.getEmail();
        this.password = microsoftAccount.getPassword();
        this.accessToken = microsoftAccount.accessToken;
        this.refreshToken = microsoftAccount.refreshToken;
        this.saveAccount = true;
    }

    public MicrosoftLogin(String email, String password, String accessToken, String refreshToken, boolean saveAccount) {
        this.email = email;
        this.password = password;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.saveAccount = saveAccount;
    }

    public boolean login() {
        String code;
        if (accessToken == null || refreshToken == null || accessToken.isEmpty() || refreshToken.isEmpty()) {
            code = getLoginCode(email, password);
        } else {
            if (verifyStore(accessToken, refreshToken)) {//quick login
                return true;
            } else {
                code = getLoginCode(email, password);
            }
        }
        if (code != null) {
            return getAccessToken(code);
        }
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "\247cInvalid login info";
        }
        return false;
    }

    private boolean getAccessToken(String code) {
        JexClient.INSTANCE.getLogger().info(code);
        JexClient.INSTANCE.getLogger().info("Grabbing Access Token");
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Grabbing Access Token";
        }
        try {
            URI uri = new URI("https://login.live.com/oauth20_token.srf");
            Map<String, String> map = Maps.newHashMap();
            map.put("client_id", "00000000402b5328");
            map.put("code", code);
            map.put("grant_type", "authorization_code");
            map.put("redirect_uri", "https://login.live.com/oauth20_desktop.srf");
            map.put("scope", "service::user.auth.xboxlive.com::MBI_SSL");

            String r = WebHelper.INSTANCE.sendPOST(new URL(uri.toString()), map);

            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(r, JsonObject.class);
            if (jsonObject.get("access_token") != null) {
                accessToken = jsonObject.get("access_token").getAsString();
                refreshToken = jsonObject.get("refresh_token").getAsString();
                return authenticateXboxLive(accessToken, refreshToken);
            } else {
                if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                    accountManagerScreen.outputString = "Error Grabbing Access Token";
                }
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean authenticateXboxLive(String accessToken, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Authenticating Xbox Live");
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Authenticating Xbox Live";
        }
        Map<Object, Object> data = Map.of(
                "Properties", Map.of(
                        "AuthMethod", "RPS",
                        "SiteName", "user.auth.xboxlive.com",
                        "RpsTicket", accessToken
                ),
                "RelyingParty", "http://auth.xboxlive.com",
                "TokenType", "JWT"
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String response = WebHelper.INSTANCE.sendPOST("https://user.auth.xboxlive.com/user/authenticate", JsonHelper.INSTANCE.gson.toJson(data), headers);
        if (response != null) {
            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(response, JsonObject.class);
            String xblToken = jsonObject.get("Token").getAsString();
            return xstsAuthenticate(xblToken, refreshToken);
        } else {
            if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Error Authenticating Xbox Live";
            }
        }
        return false;
    }

    private boolean xstsAuthenticate(String token, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Authenticating XSTS");
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Authenticating XSTS";
        }

        Map<Object, Object> data = Map.of(
                "Properties", Map.of(
                        "SandboxId", "RETAIL",
                        "UserTokens", List.of(token)
                ),
                "RelyingParty", "rp://api.minecraftservices.com/",
                "TokenType", "JWT"
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String response = WebHelper.INSTANCE.sendPOST("https://xsts.auth.xboxlive.com/xsts/authorize", JsonHelper.INSTANCE.gson.toJson(data), headers);

        if (response != null) {
            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(response, JsonObject.class);
            String xblXsts = jsonObject.get("Token").getAsString();
            JsonObject claims = jsonObject.get("DisplayClaims").getAsJsonObject();
            JsonArray xui = claims.get("xui").getAsJsonArray();
            String uhs = (xui.get(0)).getAsJsonObject().get("uhs").getAsString();
            return authenticateMinecraft(uhs, xblXsts, refreshToken);
        } else {
            if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Error Authenticating XSTS";
            }
        }
        return false;
    }

    private boolean authenticateMinecraft(String userHash, String newToken, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Authenticating with Minecraft");
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Authenticating with Minecraft";
        }
        Map<Object, Object> data = Map.of(
                "identityToken", "XBL3.0 x=" + userHash + ";" + newToken
        );
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json");
        String response = WebHelper.INSTANCE.sendPOST("https://api.minecraftservices.com/authentication/login_with_xbox", JsonHelper.INSTANCE.gson.toJson(data), headers);

        if (response != null) {
            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(response, JsonObject.class);
            String mcAccessToken = jsonObject.get("access_token").getAsString();
            return verifyStore(mcAccessToken, refreshToken);
        } else {
            if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Error Authenticating with Minecraft";
            }
        }
        return false;
    }

    private boolean verifyStore(String token, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Verifying with MS Store");
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Verifying with MS Store";
        }
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + token);
        String resp = WebHelper.INSTANCE.readURL("https://api.minecraftservices.com/entitlements/mcstore", header);
        if (resp != null) {
            return verifyProfile(token, refreshToken);
        } else {
            if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Error Verifying MS Store";
            }
        }
        return false;
    }

    private boolean verifyProfile(String token, String refresh) {
        JexClient.INSTANCE.getLogger().info("Grabbing Minecraft profile");
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Grabbing Minecraft Profile";
            if (accountManagerScreen.getSelected() != null && accountManagerScreen.getSelected().getAccount() instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                microsoftAccount.loginCount++;
                microsoftAccount.lastUsed = System.currentTimeMillis();
            }
        }
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + token);
        String resp = WebHelper.INSTANCE.readURL("https://api.minecraftservices.com/minecraft/profile", header);

        if (resp != null) {
            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(resp, JsonObject.class);
            String name = jsonObject.get("name").getAsString();
            String uuid = jsonObject.get("id").getAsString();
            setSession(name, uuid, token, refresh);
            return true;
        } else {
            if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Error Grabbing Minecraft Profile";
            }
        }
        return false;
    }

    private void setSession(String name, String uuid, String token, String refresh) {
        if (saveAccount) {
            MinecraftAccount mcAccount = MinecraftAccountManager.INSTANCE.getAccount(name);
            if (mcAccount != null) {
                if (mcAccount instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                    microsoftAccount.setUsername(name);
                    microsoftAccount.accessToken = token;
                    microsoftAccount.refreshToken = refresh;
                    microsoftAccount.uuid = uuid;
                } else
                    MinecraftAccountManager.INSTANCE.getAccounts().remove(mcAccount);
            } else {
                if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen && accountManagerScreen.getSelected() != null && accountManagerScreen.getSelected().getAccount() instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                    microsoftAccount.setUsername(name);
                    microsoftAccount.accessToken = token;
                    microsoftAccount.refreshToken = refresh;
                    microsoftAccount.uuid = uuid;
                } else {
                    MinecraftAccount.MicrosoftAccount microsoftAccount = new MinecraftAccount.MicrosoftAccount(name, email, password, token, refresh, uuid);
                    MinecraftAccountManager.INSTANCE.getAccounts().add(microsoftAccount);
                }
            }
            AltFile.write();
        }
        if (Wrapper.INSTANCE.getMinecraft() == null)
            return;
        Wrapper.INSTANCE.getMinecraft().execute(() -> {
            Wrapper.INSTANCE.getIMinecraft().setSession(new Session(name, uuid, token, "mojang"));
            if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AddAccountScreen || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DirectLoginScreen)
                Wrapper.INSTANCE.getMinecraft().openScreen(new AccountManagerScreen());
            else if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Logged in as " + name;
            }
        });
        JexClient.INSTANCE.getLogger().info("Login success. Name: " + name);
    }

    private String getLoginCode(String email, String password) {
        JexClient.INSTANCE.getLogger().info("Grabbing login code");
        if (Wrapper.INSTANCE.getMinecraft() != null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Grabbing login code";
        }
        try {
            String loginPPFT;
            String loginUrl;
            URL url = new URL("https://login.live.com/oauth20_authorize.srf?redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=code&locale=en&client_id=00000000402b5328");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getResponseCode() == 200 ? httpURLConnection.getInputStream() : httpURLConnection.getErrorStream();

            String loginCookie = httpURLConnection.getHeaderField("set-cookie");

            String responseData = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining());
            Matcher bodyMatcher = Pattern.compile("sFTTag:[ ]?'.*value=\"(.*)\"/>'").matcher(responseData);
            if (bodyMatcher.find()) {
                loginPPFT = bodyMatcher.group(1);
            } else {
                JexClient.INSTANCE.getLogger().info("Error grabbing login code");
                return null;
            }

            bodyMatcher = Pattern.compile("urlPost:[ ]?'(.+?(?='))").matcher(responseData);
            if (bodyMatcher.find()) {
                loginUrl = bodyMatcher.group(1);
            } else {
                JexClient.INSTANCE.getLogger().info("Error grabbing login code");
                return null;
            }

            if (loginCookie == null || loginPPFT == null || loginUrl == null) {
                JexClient.INSTANCE.getLogger().info("Error grabbing login code");
                return null;
            }

            Map<String, String> requestData = new HashMap<>();

            requestData.put("login", email);
            requestData.put("loginfmt", email);
            requestData.put("passwd", password);
            requestData.put("PPFT", loginPPFT);

            String postData = encodeURL(requestData);

            byte[] data = postData.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection connection = (HttpURLConnection) new URL(loginUrl).openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            connection.setRequestProperty("Content-Length", String.valueOf(data.length));
            connection.setRequestProperty("Cookie", loginCookie);
            connection.setConnectTimeout(10 * 1000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(data);
            }

            if (connection.getResponseCode() != 200 || connection.getURL().toString().equals(loginUrl)) {
                JexClient.INSTANCE.getLogger().info("Error grabbing login code");
                return null;
            }

            Pattern pattern = Pattern.compile("[?|&]code=([\\w.-]+)");

            Matcher tokenMatcher = pattern.matcher(URLDecoder.decode(connection.getURL().toString(), StandardCharsets.UTF_8.name()));
            if (tokenMatcher.find()) {
                return tokenMatcher.group(1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        JexClient.INSTANCE.getLogger().info("Error grabbing login code");
        return null;
    }

    private String encodeURL(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                    URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
            ));
        }
        return sb.toString();
    }
}
