package me.dustin.jex.helper.network.jexsite;

import com.google.common.primitives.Longs;
import com.google.gson.JsonObject;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import me.dustin.jex.JexClient;
import me.dustin.jex.gui.site.JexWebsiteScreen;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.WebHelper;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.text.Text;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JexSitePacketListener extends ClientLoginNetworkHandler {
    private final ClientConnection connection;
    public JexSitePacketListener(ClientConnection connection) {
        super(connection, null, null, null);
        this.connection = connection;
    }

    @Override
    public void onHello(LoginHelloS2CPacket packet) {
        LoginKeyC2SPacket loginKeyC2SPacket;
        Cipher cipher2;
        Cipher cipher;
        String string;
        try {
            SecretKey secretKey = NetworkEncryptionUtils.generateSecretKey();
            PublicKey publicKey = packet.getPublicKey();
            string = new BigInteger(NetworkEncryptionUtils.computeServerId(packet.getServerId(), publicKey, secretKey)).toString(16);
            cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
            cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
            byte[] bs = packet.getNonce();
            Signer signer = Wrapper.INSTANCE.getMinecraft().getProfileKeys().getSigner();
            if (signer == null) {
                loginKeyC2SPacket = new LoginKeyC2SPacket(secretKey, publicKey, bs);
            } else {
                long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
                byte[] cs = signer.sign(updater -> {
                    updater.update(bs);
                    updater.update(Longs.toByteArray(l));
                });
                loginKeyC2SPacket = new LoginKeyC2SPacket(secretKey, publicKey, l, cs);
            }
        }
        catch (Exception exception) {
            throw new IllegalStateException("Protocol error", exception);
        }
        Text text = this.joinServer(string);
        if (text != null) {
            if (Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry() != null && Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry().isLocal()) {
                JexClient.INSTANCE.getLogger().warn(text.getString());
            } else {
                this.connection.disconnect(text);
                return;
            }
        }
        this.connection.send(loginKeyC2SPacket);
        this.connection.setupEncryption(cipher, cipher2);
    }

    @Override
    public void onDisconnect(LoginDisconnectS2CPacket packet) {
        this.connection.disconnect(packet.getReason());
        String reason = packet.getReason().getString();
        if (reason.startsWith("Your token is:")) {
            String token = reason.split(":")[1].split("\n")[0].substring(5, 11);
            if (JexSiteHelper.INSTANCE.linkAccount(token)) {
                JexClient.INSTANCE.getLogger().info("Linked account!");
                JexSiteHelper.INSTANCE.updateUUID();
                if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof JexWebsiteScreen jexWebsiteScreen) {
                    jexWebsiteScreen.setTask(unused -> {
                        Wrapper.INSTANCE.getMinecraft().setScreen(new JexWebsiteScreen(new TitleScreen()));
                    });
                }
            } else {
                if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof JexWebsiteScreen jexWebsiteScreen) {
                    jexWebsiteScreen.setTask(unused -> {
                        Wrapper.INSTANCE.getMinecraft().setScreen(new JexWebsiteScreen(new TitleScreen(), Text.translatable("jex.site.connect_error").getString()));
                    });
                }
            }
        }
    }

    @Override
    public void onDisconnected(Text reason) {
    }

    private Text joinServer(String serverId) {
        JsonObject request = new JsonObject();
        request.addProperty("accessToken", Wrapper.INSTANCE.getMinecraft().getSession().getAccessToken());
        request.addProperty("selectedProfile", Wrapper.INSTANCE.getMinecraft().getSession().getUuid());
        request.addProperty("serverId", serverId);
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        WebHelper.HttpResponse resp = WebHelper.INSTANCE.httpRequest("https://sessionserver.mojang.com/session/minecraft/join", request.toString(), header, "POST");
        if (resp.responseCode() != 204) {
            return Text.translatable("disconnect.loginFailedInfo", resp);
        }
        return null;
    }
}
