package me.dustin.jex.helper.network.login;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicrosoftLogin {

    private boolean saveAccount;
    public MicrosoftLogin(boolean saveAccount) {
        this.saveAccount = saveAccount;
    }

    public boolean login(String accessToken, String refreshToken) {
        try {
            verifyStore(accessToken, refreshToken);
            JexClient.INSTANCE.getLogger().info("Refreshing login tokens");
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Refreshing login tokens";
            }
            URI uri = new URI("https://login.live.com/oauth20_token.srf");

            Map<Object, Object> map = Maps.newHashMap();
            map.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
            map.put("refresh_token", refreshToken);
            map.put("grant_type", "refresh_token");
            map.put("redirect_uri", "http://localhost:59125");

            AtomicBoolean pass = new AtomicBoolean(false);
            HttpRequest request = HttpRequest.newBuilder(uri).POST(ofFormData(map)).build();
            HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String body = resp.body();
                    JsonObject object = JsonHelper.INSTANCE.gson.fromJson(body, JsonObject.class);
                    String accessToken1 = object.get("access_token").getAsString();
                    String refreshToken1 = object.get("refresh_token").getAsString();
                    pass.set(authenticateXboxLive(accessToken1, refreshToken1));
                }
            });
            return pass.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void refreshTokens(MinecraftAccount.MicrosoftAccount microsoftAccount) {
        try {
            JexClient.INSTANCE.getLogger().info("Refreshing login tokens");
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Refreshing login tokens";
            }
            URI uri = new URI("https://login.live.com/oauth20_token.srf");

            Map<Object, Object> map = Maps.newHashMap();
            map.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
            map.put("refresh_token", microsoftAccount.refreshToken);
            map.put("grant_type", "refresh_token");
            map.put("redirect_uri", "http://localhost:59125");

            HttpRequest request = HttpRequest.newBuilder(uri).POST(ofFormData(map)).build();
            HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String body = resp.body();
                    JsonObject object = JsonHelper.INSTANCE.gson.fromJson(body, JsonObject.class);
                    microsoftAccount.accessToken = object.get("access_token").getAsString();
                    microsoftAccount.refreshToken = object.get("refresh_token").getAsString();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    HttpServer server;
    public void startLoginProcess() {
        new Thread(() -> {
            try {
                String done = "<html><body><h1 style='color:blue'>Jex Client</h1><br><p style='font_size=16px'>You may now close this window</p></body></html>";
                server = HttpServer.create(new InetSocketAddress(59125), 0);
                server.createContext("/", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        Wrapper.INSTANCE.getMinecraft().execute(() -> GLFW.glfwFocusWindow(Wrapper.INSTANCE.getWindow().getHandle()));
                        exchange.getResponseHeaders().add("Location", "http://localhost:59125/end");
                        exchange.sendResponseHeaders(302, -1L);
                        String query = exchange.getRequestURI().getQuery();
                        if (query == null) {
                            JexClient.INSTANCE.getLogger().error("query=null error");
                        } else if (query.startsWith("code=")) {
                            try {
                                getAccessToken(query.replace("code=", ""));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (query.equalsIgnoreCase("error=access_denied&error_description=The user has denied access to the scope requested by the client application.")) {
                            JexClient.INSTANCE.getLogger().error("Access request denied");
                        } else {
                            JexClient.INSTANCE.getLogger().error("Something went wrong");
                        }
                    }
                });
                server.createContext("/end", new HttpHandler() {
                    public void handle(HttpExchange ex) throws IOException {
                        try {
                            byte[] b = done.getBytes(StandardCharsets.UTF_8);
                            ex.getResponseHeaders().put("Content-Type", Arrays.asList(new String[] { "text/html; charset=UTF-8" }));
                            ex.sendResponseHeaders(200, b.length);
                            OutputStream os = ex.getResponseBody();
                            os.write(b);
                            os.flush();
                            os.close();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        stopLoginProcess();
                    }
                });
                server.start();
                Util.getOperatingSystem().open("https://login.live.com/oauth20_authorize.srf?client_id=54fd49e4-2103-4044-9603-2b028c814ec3&response_type=code&scope=XboxLive.signin%20XboxLive.offline_access&redirect_uri=http://localhost:59125&prompt=consent");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void stopLoginProcess() {
        if (server != null)
            server.stop(0);
    }

    private void getAccessToken(String code) throws URISyntaxException, MalformedURLException {
        JexClient.INSTANCE.getLogger().info("Grabbing Access Token");
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Grabbing Access Token";
        }
        URI uri = new URI("https://login.live.com/oauth20_token.srf");
        Map<String, String> map = Maps.newHashMap();
        map.put("client_id", "54fd49e4-2103-4044-9603-2b028c814ec3");
        map.put("code", code);
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://localhost:59125");
        map.put("scope", "XboxLive.signin XboxLive.offline_access");

        String r = WebHelper.INSTANCE.sendPOST(new URL(uri.toString()), map);

        JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(r, JsonObject.class);
        if (jsonObject.get("access_token") != null) {
            String accessToken = jsonObject.get("access_token").getAsString();
            String refreshToken = jsonObject.get("refresh_token").getAsString();
            authenticateXboxLive(accessToken, refreshToken);
        } else {
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Error Grabbing Access Token";
            }
        }
    }

    private boolean authenticateXboxLive(String accessToken, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Authenticating Xbox Live");
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Authenticating Xbox Live";
        }

        try {
            URI uri = new URI("https://user.auth.xboxlive.com/user/authenticate");
            Map<Object, Object> data = Map.of(
                    "Properties", Map.of(
                            "AuthMethod", "RPS",
                            "SiteName", "user.auth.xboxlive.com",
                            "RpsTicket", "d="+accessToken
                    ),
                    "RelyingParty", "http://auth.xboxlive.com",
                    "TokenType", "JWT"
            );
            AtomicBoolean pass = new AtomicBoolean(false);
            HttpRequest request = HttpRequest.newBuilder(uri).header("Content-Type", "application/json").header("Accept", "application/json").POST(ofJSONData(data)).build();
            HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String body = resp.body();
                    JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(body, JsonObject.class);
                    String xblToken = jsonObject.get("Token").getAsString();
                    pass.set(xstsAuthenticate(xblToken, refreshToken));
                } else {
                    JexClient.INSTANCE.getLogger().info("Status code: " + resp.statusCode() + " : " + resp.body());
                    if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                        accountManagerScreen.outputString = "Error Authenticating Xbox Live";
                    }
                }
            });
            return pass.get();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean xstsAuthenticate(String token, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Authenticating XSTS");
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Authenticating XSTS";
        }
        try {
            URI uri = new URI("https://xsts.auth.xboxlive.com/xsts/authorize");

            Map<Object, Object> data = Map.of(
                    "Properties", Map.of(
                            "SandboxId", "RETAIL",
                            "UserTokens", List.of(token)
                    ),
                    "RelyingParty", "rp://api.minecraftservices.com/",
                    "TokenType", "JWT"
            );
            AtomicBoolean pass = new AtomicBoolean(false);
            HttpRequest request = HttpRequest.newBuilder(uri).header("Content-Type", "application/json").header("Accept", "application/json").POST(ofJSONData(data)).build();

            HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String body = resp.body();
                    JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(body, JsonObject.class);
                    String xblXsts = jsonObject.get("Token").getAsString();
                    JsonObject claims = jsonObject.get("DisplayClaims").getAsJsonObject();
                    JsonArray xui = claims.get("xui").getAsJsonArray();
                    String uhs = (xui.get(0)).getAsJsonObject().get("uhs").getAsString();
                    pass.set(authenticateMinecraft(uhs, xblXsts, refreshToken));
                } else {
                    JexClient.INSTANCE.getLogger().info(resp.body());
                    if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                        accountManagerScreen.outputString = "Error Authenticating XSTS";
                    }
                }
            });
            return pass.get();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticateMinecraft(String userHash, String newToken, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Authenticating with Minecraft");
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Authenticating with Minecraft";
        }
        try {
            URI uri = new URI("https://api.minecraftservices.com/authentication/login_with_xbox");
            Map<Object, Object> data = Map.of(
                    "identityToken", "XBL3.0 x=" + userHash + ";" + newToken
            );
            AtomicBoolean pass = new AtomicBoolean(false);

            HttpRequest request = HttpRequest.newBuilder(uri).header("Content-Type", "application/json").header("Accept", "application/json").POST(ofJSONData(data)).build();
            HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String body = resp.body();
                    JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(body, JsonObject.class);
                    String mcAccessToken = jsonObject.get("access_token").getAsString();
                    pass.set(verifyStore(mcAccessToken, refreshToken));
                } else {
                    JexClient.INSTANCE.getLogger().info(resp.body());
                    if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                        accountManagerScreen.outputString = "Error Authenticating with Minecraft";
                    }
                }
            });
            return pass.get();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean verifyStore(String token, String refreshToken) {
        JexClient.INSTANCE.getLogger().info("Verifying with MS Store");
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Verifying with MS Store";
        }
        try {
            URI uri = new URI("https://api.minecraftservices.com/entitlements/mcstore");
            AtomicBoolean pass = new AtomicBoolean(false);

            HttpRequest request = HttpRequest.newBuilder(uri).header("Authorization", "Bearer " + token).GET().build();
            HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    pass.set(verifyProfile(token, refreshToken));
                } else {
                    JexClient.INSTANCE.getLogger().info(resp.body());
                    if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                        accountManagerScreen.outputString = "Error Verifying MS Store";
                    }
                }
            });
            return pass.get();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean verifyProfile(String token, String refresh) {
        JexClient.INSTANCE.getLogger().info("Grabbing Minecraft profile");
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
            accountManagerScreen.outputString = "Grabbing Minecraft Profile";
            if (accountManagerScreen.getSelected() != null && accountManagerScreen.getSelected().getAccount() instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                microsoftAccount.loginCount++;
                microsoftAccount.lastUsed = System.currentTimeMillis();
            }
        }
        try {
            URI uri = new URI("https://api.minecraftservices.com/minecraft/profile");
            AtomicBoolean pass = new AtomicBoolean(false);

            HttpRequest request = HttpRequest.newBuilder(uri).header("Authorization", "Bearer " + token).GET().build();
            HttpClient.newBuilder().build().sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String body = resp.body();
                    JsonObject jsonObject =  JsonHelper.INSTANCE.gson.fromJson(body, JsonObject.class);
                    String name = jsonObject.get("name").getAsString();
                    String uuid = jsonObject.get("id").getAsString();
                    setSession(name, uuid, token, refresh);
                    pass.set(true);
                } else {
                    JexClient.INSTANCE.getLogger().info(resp.body());
                    if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                        accountManagerScreen.outputString = "Error Grabbing Minecraft Profile";
                    }
                }
            });
            return pass.get();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
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
                if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen && accountManagerScreen.getSelected() != null && accountManagerScreen.getSelected().getAccount() instanceof MinecraftAccount.MicrosoftAccount microsoftAccount) {
                    microsoftAccount.setUsername(name);
                    microsoftAccount.accessToken = token;
                    microsoftAccount.refreshToken = refresh;
                    microsoftAccount.uuid = uuid;
                } else {
                    MinecraftAccount.MicrosoftAccount microsoftAccount = new MinecraftAccount.MicrosoftAccount(name, token, refresh, uuid);
                    MinecraftAccountManager.INSTANCE.getAccounts().add(microsoftAccount);
                }
            }
            AltFile.write();
        }
        Wrapper.INSTANCE.getMinecraft().execute(() -> {
            Wrapper.INSTANCE.getIMinecraft().setSession(new Session(name, uuid, token, "mojang"));
            if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AddAccountScreen || Wrapper.INSTANCE.getMinecraft().currentScreen instanceof DirectLoginScreen)
                Wrapper.INSTANCE.getMinecraft().openScreen(new AccountManagerScreen());
            else if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AccountManagerScreen accountManagerScreen) {
                accountManagerScreen.outputString = "Logged in as " + name;
            }
        });
        JexClient.INSTANCE.getLogger().info("Login success. Name: " + name);
    }

    private HttpRequest.BodyPublisher ofJSONData(Map<Object, Object> data) {
        return HttpRequest.BodyPublishers.ofString(JsonHelper.INSTANCE.prettyGson.toJson(data).toString());
    }

    private HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
}
