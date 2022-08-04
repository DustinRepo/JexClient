package me.dustin.jex.helper.network.jexsite;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import me.dustin.irc.network.pipeline.PacketSizePrepender;
import me.dustin.irc.network.pipeline.PacketSplitterHandler;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MinecraftServerAddress;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.network.*;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum JexSiteHelper {
    INSTANCE;
    private JexUser user;
    public String login(String username, String password) {
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        String response = WebHelper.INSTANCE.httpRequest(JexClient.INSTANCE.getBaseUrl() + "/inc/client/client-login.inc.php", data, null, "POST").data();
        try {
            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(response, JsonObject.class);
            if (jsonObject.has("error"))
                return jsonObject.get("error").getAsString();
            else {
                if (jsonObject.has("name") && jsonObject.has("id") && jsonObject.has("uuid") && jsonObject.has("token") && jsonObject.has("donator")) {
                    user = new JexUser(jsonObject.get("name").getAsString(), jsonObject.get("id").getAsInt(), jsonObject.get("uuid").getAsString(), jsonObject.get("token").getAsString(), jsonObject.get("donator").getAsBoolean());
                    return "";
                }
            }
        } catch (Exception e) {}
        return "Error.";
    }

    public String register(String username, String email, String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm))
            return "Passwords do not match.";
        if (username.length() < 4)
            return "Username too short.";
        if (password.length() < 8)
            return "Password too short.";
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);
        data.put("password", password);
        data.put("passwordConfirm", passwordConfirm);
        String response = WebHelper.INSTANCE.httpRequest(JexClient.INSTANCE.getBaseUrl() + "/inc/client/client-register.inc.php", data, null, "POST").data();
        try {
            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(response, JsonObject.class);
            if (jsonObject.has("error"))
                return jsonObject.get("error").getAsString();
            else {
                if (jsonObject.has("name") && jsonObject.has("id") && jsonObject.has("uuid") && jsonObject.has("token") && jsonObject.has("donator")) {
                    user = new JexUser(jsonObject.get("name").getAsString(), jsonObject.get("id").getAsInt(), jsonObject.get("uuid").getAsString(), jsonObject.get("token").getAsString(), jsonObject.get("donator").getAsBoolean());
                    return "";
                }
            }
        } catch (Exception e) {}
        return "Error.";
    }

    public boolean linkAccount(String token) {
        Map<String, String> data = new HashMap<>();
        data.put("auth", user.token());
        data.put("token", token);
        data.put("userId", "%d".formatted(user.id()));
        String response = WebHelper.INSTANCE.httpRequest(JexClient.INSTANCE.getBaseUrl() + "/inc/client/client-link-account.inc.php", data, null, "POST").data();
        try {
            JsonObject object = JsonHelper.INSTANCE.gson.fromJson(response, JsonObject.class);
            if (object.has("error"))
                JexClient.INSTANCE.getLogger().error(object.get("error").getAsString());
            return object.has("message");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String pushSetting(String setting, Object value) {
        Map<String, String> data = new HashMap<>();
        data.put("auth", user.token());
        data.put("userId", String.valueOf(user.id()));
        data.put(setting, String.valueOf(value));
        String response = WebHelper.INSTANCE.httpRequest(JexClient.INSTANCE.getBaseUrl() + "/inc/client/client-push-settings.inc.php", data, null, "POST").data();
        System.out.println(response);
        try {
            JsonObject object = JsonHelper.INSTANCE.gson.fromJson(response, JsonObject.class);
            if (object.has("error"))
                return object.get("error").getAsString();
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error.";
        }
    }

    public void connectAndLinkAccount() {
        Thread thread = new Thread("Jex Site Connector"){
            @Override
            public void run() {
                ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
                MinecraftServerAddress minecraftServerAddress = MinecraftServerAddress.resolve("localhost", 25565);
                Bootstrap bootstrap = new Bootstrap().group(new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build()));
                bootstrap = bootstrap.handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        clientConnection.setPacketListener(new JexSitePacketListener(clientConnection));
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                        channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                        channel.pipeline().addLast("splitter", new PacketSplitterHandler());
                        channel.pipeline().addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND));
                        channel.pipeline().addLast("prepender", new PacketSizePrepender());
                        channel.pipeline().addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND));
                        channel.pipeline().addLast("packet_handler", clientConnection);
                    }
                });
                bootstrap = bootstrap.channel(NioSocketChannel.class);
                try {
                    bootstrap.connect(minecraftServerAddress.getIp(), minecraftServerAddress.getPort()).sync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                clientConnection.send(new HandshakeC2SPacket(minecraftServerAddress.getIp(), minecraftServerAddress.getPort(), NetworkState.LOGIN));
                clientConnection.send(new LoginHelloC2SPacket(Wrapper.INSTANCE.getMinecraft().getSession().getUsername(), Wrapper.INSTANCE.getMinecraft().getProfileKeys().getPublicKeyData(), Optional.ofNullable(Wrapper.INSTANCE.getMinecraft().getSession().getUuidOrNull())));
            }
        };
        thread.start();
    }

    public JexUser getUser() {
        return user;
    }

    public void updateUUID() {
        JexUser original = user;
        this.user = new JexUser(original.name(), original.id(), Wrapper.INSTANCE.getMinecraft().getSession().getUuid().replace("-", ""), original.token(), original.donator());
    }

    public record JexUser(String name, int id, String uuid, String token, boolean donator) {}
}
