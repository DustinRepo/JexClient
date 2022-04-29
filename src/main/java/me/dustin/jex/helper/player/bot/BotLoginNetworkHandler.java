package me.dustin.jex.helper.player.bot;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BotLoginNetworkHandler extends ClientLoginNetworkHandler {
    private final GameProfile gameProfile;
    private final PlayerBot playerBot;
    public BotLoginNetworkHandler(ClientConnection connection, MinecraftClient client, @Nullable Screen parentGui, Consumer<Text> statusConsumer, GameProfile gameProfile, PlayerBot playerBot) {
        super(connection, client, parentGui, statusConsumer);
        this.gameProfile = gameProfile;
        this.playerBot = playerBot;
    }

    @Override
    public void onHello(LoginHelloS2CPacket packet) {
        LoginKeyC2SPacket loginKeyC2SPacket;
        Cipher cipher2;
        Cipher cipher;
        String string;
        try {
            SecretKey secretKey = NetworkEncryptionUtils.generateKey();
            PublicKey publicKey = packet.getPublicKey();
            string = new BigInteger(NetworkEncryptionUtils.generateServerId(packet.getServerId(), publicKey, secretKey)).toString(16);
            cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
            cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
            loginKeyC2SPacket = new LoginKeyC2SPacket(secretKey, publicKey, packet.getNonce());
        }
        catch (NetworkEncryptionException secretKey) {
            throw new IllegalStateException("Protocol error", secretKey);
        }
        ChatHelper.INSTANCE.addRawMessage(Text.translatable("connect.authorizing"));
        NetworkUtils.EXECUTOR.submit(() -> {
            Text text = contactSessionServers(string);
            if (text != null) {
                if (Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry() != null && Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().isLocal()) {
                    ChatHelper.INSTANCE.addClientMessage("WARN: " + text.getString());
                } else {
                    this.onDisconnected(text);
                    return;
                }
            }
            ChatHelper.INSTANCE.addRawMessage(Text.translatable("connect.encrypting"));
            this.playerBot.getClientConnection().send(loginKeyC2SPacket, future -> this.playerBot.getClientConnection().setupEncryption(cipher, cipher2));
        });
    }

    public Text contactSessionServers(String serverHash) {
        JsonObject request = new JsonObject();
        request.addProperty("accessToken", playerBot.getSession().getAccessToken());
        request.addProperty("selectedProfile", playerBot.getSession().getUuid());
        request.addProperty("serverId", serverHash);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        WebHelper.HttpResponse resp = WebHelper.INSTANCE.httpRequest("https://sessionserver.mojang.com/session/minecraft/join", request.toString(), header, "POST");
        if (resp.responseCode() != 204) {
            return Text.of("Could not verify username!");
        }
        return null;
    }

    @Override
    public void onSuccess(LoginSuccessS2CPacket packet) {
        super.onSuccess(packet);
        this.getConnection().setPacketListener(new BotClientPlayNetworkHandler(Wrapper.INSTANCE.getMinecraft(), null, this.getConnection(), this.gameProfile, Wrapper.INSTANCE.getMinecraft().createTelemetrySender(), playerBot));
        playerBot.setConnected(true);
        if (NetworkHelper.INSTANCE.getStoredSession() != null) {
            Wrapper.INSTANCE.getIMinecraft().setSession(NetworkHelper.INSTANCE.getStoredSession());
            NetworkHelper.INSTANCE.setStoredSession(null);
        }
    }

    @Override
    public void onDisconnected(Text reason) {
        super.onDisconnected(reason);
        playerBot.disconnect();
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " could not connect for reason: " + Formatting.RED + reason.getString());
    }

    @Override
    public void onDisconnect(LoginDisconnectS2CPacket packet) {
        super.onDisconnect(packet);
        playerBot.disconnect();
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " could not connect for reason: " + Formatting.RED + packet.getReason().getString());
    }
}
