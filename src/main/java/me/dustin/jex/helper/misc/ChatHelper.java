package me.dustin.jex.helper.misc;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Instant;

public enum ChatHelper {
    INSTANCE;

    public void sendChatMessage(String chat) {
        Instant instant = Instant.now();
        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(instant, chat, sigForMessage(instant, chat)));
    }

    private NetworkEncryptionUtils.SignatureData sigForMessage(Instant instant, String string) {
        try {
            Signature signature = Wrapper.INSTANCE.getMinecraft().method_43590().method_43599();
            if (signature != null) {
                long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
                NetworkEncryptionUtils.updateSignature(signature, l, Wrapper.INSTANCE.getLocalPlayer().getUuid(), instant, string);
                return new NetworkEncryptionUtils.SignatureData(l, signature.sign());
            }
        } catch (GeneralSecurityException var6) {
            JexClient.INSTANCE.getLogger().error("Failed to sign chat message {}", instant, var6);
        }

        return NetworkEncryptionUtils.SignatureData.field_39040;
    }

    public void addClientMessage(String message) {
        Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud().addMessage(Text.of(String.format("%s[%sJex%s]%s: %s%s", Formatting.DARK_GRAY, Formatting.AQUA, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY, message)));
    }

    public void addRawMessage(String message) {
        Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud().addMessage(Text.of(message));
    }

    public void addRawMessage(Text message) {
        Wrapper.INSTANCE.getMinecraft().inGameHud.getChatHud().addMessage(message);
    }
}
