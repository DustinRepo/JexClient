package me.dustin.jex.helper.player.bot;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.HttpUtil;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BotLoginNetworkHandler extends ClientHandshakePacketListenerImpl {
    private final GameProfile gameProfile;
    private final PlayerBot playerBot;
    public BotLoginNetworkHandler(Connection connection, Minecraft client, @Nullable Screen parentGui, Consumer<Component> statusConsumer, GameProfile gameProfile, PlayerBot playerBot) {
        super(connection, client, parentGui, statusConsumer);
        this.gameProfile = gameProfile;
        this.playerBot = playerBot;
    }

    @Override
    public void handleHello(ClientboundHelloPacket packet) {
        ServerboundKeyPacket loginKeyC2SPacket;
        Cipher cipher2;
        Cipher cipher;
        String string;
        try {
            SecretKey secretKey = Crypt.generateSecretKey();
            PublicKey publicKey = packet.getPublicKey();
            string = new BigInteger(Crypt.digestData(packet.getServerId(), publicKey, secretKey)).toString(16);
            cipher = Crypt.getCipher(2, secretKey);
            cipher2 = Crypt.getCipher(1, secretKey);
            loginKeyC2SPacket = new ServerboundKeyPacket(secretKey, publicKey, packet.getNonce());
        }
        catch (CryptException secretKey) {
            throw new IllegalStateException("Protocol error", secretKey);
        }
        ChatHelper.INSTANCE.addRawMessage(Component.translatable("connect.authorizing"));
        HttpUtil.DOWNLOAD_EXECUTOR.submit(() -> {
            Component text = contactSessionServers(string);
            if (text != null) {
                if (Wrapper.INSTANCE.getMinecraft().getCurrentServer() != null && Wrapper.INSTANCE.getMinecraft().getCurrentServer().isLan()) {
                    ChatHelper.INSTANCE.addClientMessage("WARN: " + text.getString());
                } else {
                    this.onDisconnect(text);
                    return;
                }
            }
            ChatHelper.INSTANCE.addRawMessage(Component.translatable("connect.encrypting"));
            this.playerBot.getClientConnection().send(loginKeyC2SPacket, future -> this.playerBot.getClientConnection().setEncryptionKey(cipher, cipher2));
        });
    }

    public Component contactSessionServers(String serverHash) {
        JsonObject request = new JsonObject();
        request.addProperty("accessToken", playerBot.getSession().getAccessToken());
        request.addProperty("selectedProfile", playerBot.getSession().getUuid());
        request.addProperty("serverId", serverHash);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        WebHelper.HttpResponse resp = WebHelper.INSTANCE.httpRequest("https://sessionserver.mojang.com/session/minecraft/join", request.toString(), header, "POST");
        if (resp.responseCode() != 204) {
            return Component.nullToEmpty("Could not verify username!");
        }
        return null;
    }

    @Override
    public void handleGameProfile(ClientboundGameProfilePacket packet) {
        super.handleGameProfile(packet);
        this.getConnection().setListener(new BotClientPlayNetworkHandler(Wrapper.INSTANCE.getMinecraft(), null, this.getConnection(), this.gameProfile, Wrapper.INSTANCE.getMinecraft().createTelemetryManager(), playerBot));
        playerBot.setConnected(true);
        if (NetworkHelper.INSTANCE.getStoredSession() != null) {
            Wrapper.INSTANCE.getIMinecraft().setSession(NetworkHelper.INSTANCE.getStoredSession());
            NetworkHelper.INSTANCE.setStoredSession(null);
        }
    }

    @Override
    public void onDisconnect(Component reason) {
        super.onDisconnect(reason);
        playerBot.disconnect();
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " could not connect for reason: " + ChatFormatting.RED + reason.getString());
    }

    @Override
    public void handleDisconnect(ClientboundLoginDisconnectPacket packet) {
        super.handleDisconnect(packet);
        playerBot.disconnect();
        ChatHelper.INSTANCE.addClientMessage(playerBot.getGameProfile().getName() + " could not connect for reason: " + ChatFormatting.RED + packet.getReason().getString());
    }
}
