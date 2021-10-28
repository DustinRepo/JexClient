package me.dustin.jex.helper.network.login.thealtening;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.*;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.request.JoinMinecraftServerRequest;
import com.mojang.authlib.yggdrasil.response.*;
import com.mojang.util.UUIDTypeAdapter;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;
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

public class TheAlteningSessionService extends HttpMinecraftSessionService {

    private final String SESSION_URL = "http://sessionserver.thealtening.com";
    private final String JOIN_URL = SESSION_URL + "/session/minecraft/join";
    private final String CHECK_URL = SESSION_URL + "/session/minecraft/hasJoined";

    public TheAlteningSessionService(YggdrasilAuthenticationService authenticationService) {
        super(authenticationService);
    }

    @Override
    public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException {
        final JoinMinecraftServerRequest request = new JoinMinecraftServerRequest();
        request.accessToken = authenticationToken;
        request.selectedProfile = profile.getId();
        request.serverId = serverId;

        try {
            makeRequest(new URL(JOIN_URL), request, Response.class);
        } catch (MalformedURLException | AuthenticationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        final Map<String, Object> arguments = new HashMap<>();
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
        return NetworkHelper.INSTANCE.getStoredSessionService().getTextures(profile, requireSecure);
    }

    @Override
    public GameProfile fillProfileProperties(final GameProfile profile, final boolean requireSecure) {
        //This part uses the base url but just returning the original method doesn't seem to change anything
        return NetworkHelper.INSTANCE.getStoredSessionService().fillProfileProperties(profile, requireSecure);
    }

    protected <T extends Response> T makeRequest(final URL url, final Object input, final Class<T> classOfT) throws AuthenticationException {
        return makeRequest(url, input, classOfT, null);
    }

    protected <T extends Response> T makeRequest(final URL url, final Object input, final Class<T> classOfT, @Nullable final String authentication) throws AuthenticationException {
        try {
            final String jsonResult = input == null ? getAuthenticationService().performGetRequest(url, authentication) : getAuthenticationService().performPostRequest(url, JsonHelper.INSTANCE.gson.toJson(input), "application/json");
            final T result = JsonHelper.INSTANCE.gson.fromJson(jsonResult, classOfT);
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
}
