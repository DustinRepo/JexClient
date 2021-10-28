package me.dustin.jex.helper.network.login.thealtening;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.*;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.request.JoinMinecraftServerRequest;
import com.mojang.authlib.yggdrasil.response.*;
import com.mojang.util.UUIDTypeAdapter;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.network.WebHelper;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TheAlteningSessionService extends HttpMinecraftSessionService {

    private static final String[] ALLOWED_DOMAINS = {
            ".minecraft.net",
            ".mojang.com",
    };

    private static final String[] BLOCKED_DOMAINS = {
            "education.minecraft.net",
            "bugs.mojang.com",
    };

    private final String SESSION_URL = "http://sessionserver.thealtening.com";
    private final String BASE_URL = SESSION_URL + "/session/minecraft/";
    private final String JOIN_URL = SESSION_URL + "/session/minecraft/join";
    private final String CHECK_URL = SESSION_URL + "/session/minecraft/hasJoined";
    private final PublicKey publicKey;
    private final Gson gson;
    private final LoadingCache<GameProfile, GameProfile> insecureProfiles = CacheBuilder
            .newBuilder()
            .expireAfterWrite(6, TimeUnit.HOURS)
            .build(new CacheLoader<GameProfile, GameProfile>() {
                @Override
                public GameProfile load(final GameProfile key) throws Exception {
                    return fillGameProfile(key, false);
                }
            });

    public TheAlteningSessionService(YggdrasilAuthenticationService authenticationService) {
        super(authenticationService);
        try {
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(IOUtils.toByteArray(YggdrasilMinecraftSessionService.class.getResourceAsStream("/yggdrasil_session_pubkey.der")));
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(spec);
        } catch (final Exception ignored) {
            throw new Error("Missing/invalid yggdrasil public key!");
        }
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(GameProfile.class, new GameProfileSerializer());
        builder.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer());
        builder.registerTypeAdapter(UUID.class, new UUIDTypeAdapter());
        builder.registerTypeAdapter(ProfileSearchResultsResponse.class, new ProfileSearchResultsResponse.Serializer());
        gson = builder.create();
    }

    @Override
    public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException {
        final JoinMinecraftServerRequest request = new JoinMinecraftServerRequest();
        request.accessToken = authenticationToken;
        request.selectedProfile = profile.getId();
        request.serverId = serverId;

        try {
            makeRequest(new URL(JOIN_URL), request, Response.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        final Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);

        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }
        try {
            final URL url = HttpAuthenticationService.concatenateURL(new URL(CHECK_URL), HttpAuthenticationService.buildQuery(arguments));
            final HasJoinedMinecraftServerResponse response = makeRequest(url, null, HasJoinedMinecraftServerResponse.class);

            if (response != null && response.getId() != null) {
                final GameProfile result = new GameProfile(response.getId(), user.getName());
                if (response.getProperties() != null) {
                    result.getProperties().putAll(response.getProperties());
                }
                return result;
            } else {
                return null;
            }
        } catch (final AuthenticationUnavailableException e) {
            throw e;
        } catch (final AuthenticationException ignored) {
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        final Property textureProperty = Iterables.getFirst(profile.getProperties().get("textures"), null);
        if (textureProperty == null) {
            return new HashMap<>();
        }
        if (requireSecure) {
            if (!textureProperty.hasSignature()) {
                JexClient.INSTANCE.getLogger().error("Signature is missing from textures payload");
                throw new InsecureTextureException("Signature is missing from textures payload");
            }

            if (!textureProperty.isSignatureValid(publicKey)) {
                JexClient.INSTANCE.getLogger().error("Textures payload has been tampered with (signature invalid)");
                throw new InsecureTextureException("Textures payload has been tampered with (signature invalid)");
            }
        }
        final MinecraftTexturesPayload result;
        try {
            final String json = new String(Base64.decodeBase64(textureProperty.getValue()), Charsets.UTF_8);
            result = gson.fromJson(json, MinecraftTexturesPayload.class);
        } catch (final JsonParseException e) {
            JexClient.INSTANCE.getLogger().error("Could not decode textures payload", e);
            return new HashMap<>();
        }
        if (result == null || result.getTextures() == null) {
            return new HashMap<>();
        }
        for (final Map.Entry<MinecraftProfileTexture.Type, MinecraftProfileTexture> entry : result.getTextures().entrySet()) {
            final String url = entry.getValue().getUrl();
            if (!isAllowedTextureDomain(url)) {
                JexClient.INSTANCE.getLogger().error("Textures payload contains blocked domain: {}", url);
                return new HashMap<>();
            }
        }
        return result.getTextures();
    }

    @Override
    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        if (profile.getId() == null)
            return profile;
        if (!requireSecure)
            return insecureProfiles.getUnchecked(profile);

        return fillGameProfile(profile, true);
    }

    protected <T extends Response> T makeRequest(final URL url, final Object input, final Class<T> classOfT) throws AuthenticationException {
        return makeRequest(url, input, classOfT, null);
    }

    protected <T extends Response> T makeRequest(final URL url, final Object input, final Class<T> classOfT, @Nullable final String authentication) throws AuthenticationException {
        try {
            final String jsonResult = input == null ? getAuthenticationService().performGetRequest(url, authentication) : getAuthenticationService().performPostRequest(url, gson.toJson(input), "application/json");
            final T result = gson.fromJson(jsonResult, classOfT);
            if (result == null) {
                return null;
            }
            if (StringUtils.isNotBlank(result.getError())) {
                if ("UserMigratedException".equals(result.getCause())) {
                    throw new UserMigratedException(result.getErrorMessage());
                } else if ("ForbiddenOperationException".equals(result.getError())) {
                    throw new InvalidCredentialsException(result.getErrorMessage());
                } else if ("InsufficientPrivilegesException".equals(result.getError())) {
                    throw new InsufficientPrivilegesException(result.getErrorMessage());
                } else {
                    throw new AuthenticationException(result.getErrorMessage());
                }
            }
            return result;
        } catch (final IOException | IllegalStateException | JsonParseException e) {
            throw new AuthenticationUnavailableException("Cannot contact authentication server", e);
        }
    }

    private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
        @Override
        public GameProfile deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject object = (JsonObject) json;
            final UUID id = object.has("id") ? context.<UUID>deserialize(object.get("id"), UUID.class) : null;
            final String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
            return new GameProfile(id, name);
        }

        @Override
        public JsonElement serialize(final GameProfile src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
            if (src.getId() != null) {
                result.add("id", context.serialize(src.getId()));
            }
            if (src.getName() != null) {
                result.addProperty("name", src.getName());
            }
            return result;
        }
    }

    protected GameProfile fillGameProfile(final GameProfile profile, final boolean requireSecure) {
        try {
            URL url = HttpAuthenticationService.constantURL(BASE_URL + "profile/" + UUIDTypeAdapter.fromUUID(profile.getId()));
            url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);
            final MinecraftProfilePropertiesResponse response = gson.fromJson(WebHelper.INSTANCE.readURL(url), MinecraftProfilePropertiesResponse.class);

            if (response == null) {
                JexClient.INSTANCE.getLogger().debug("Couldn't fetch profile properties for " + profile + " as the profile does not exist");
                return profile;
            } else {
                if (StringUtils.isNotBlank(response.getError())) {
                    if ("UserMigratedException".equals(response.getCause())) {
                        throw new UserMigratedException(response.getErrorMessage());
                    } else if ("ForbiddenOperationException".equals(response.getError())) {
                        throw new InvalidCredentialsException(response.getErrorMessage());
                    } else if ("InsufficientPrivilegesException".equals(response.getError())) {
                        throw new InsufficientPrivilegesException(response.getErrorMessage());
                    } else {
                        throw new AuthenticationException(response.getErrorMessage());
                    }
                }
                final GameProfile result = new GameProfile(response.getId(), response.getName());
                result.getProperties().putAll(response.getProperties());
                profile.getProperties().putAll(response.getProperties());
                JexClient.INSTANCE.getLogger().debug("Successfully fetched profile properties for " + profile);
                return result;
            }
        } catch (final AuthenticationException | IOException e) {
            JexClient.INSTANCE.getLogger().warn("Couldn't look up profile properties for " + profile, e);
            return profile;
        }
    }

    private boolean isAllowedTextureDomain(final String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (final URISyntaxException ignored) {
            throw new IllegalArgumentException("Invalid URL '" + url + "'");
        }
        final String domain = uri.getHost();
        return isDomainOnList(domain, ALLOWED_DOMAINS) && !isDomainOnList(domain, BLOCKED_DOMAINS);
    }

    private boolean isDomainOnList(final String domain, final String[] list) {
        for (final String entry : list) {
            if (domain.endsWith(entry)) {
                return true;
            }
        }
        return false;
    }
}
