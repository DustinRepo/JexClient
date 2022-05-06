package me.dustin.jex.helper.misc;

import com.google.common.primitives.Longs;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.class_7469;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;

public enum ChatHelper {
    INSTANCE;

    public void sendChatMessage(String chat) {
        Instant instant = Instant.now();
        class_7469 chatSigData = new class_7469(Wrapper.INSTANCE.getLocalPlayer().getUuid(), instant, sigForMessage(instant, chat));
        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(chat, chatSigData));
    }

    private NetworkEncryptionUtils.SignatureData sigForMessage(Instant instant, String string) {
        try {
            Signature signature = Wrapper.INSTANCE.getMinecraft().getProfileKeys().createSignatureInstance();
            if (signature != null) {
                long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
                updateSig(signature, l, Wrapper.INSTANCE.getLocalPlayer().getUuid(), instant, string);
                return new NetworkEncryptionUtils.SignatureData(l, signature.sign());
            }
        } catch (GeneralSecurityException var6) {
            JexClient.INSTANCE.getLogger().error("Failed to sign chat message {}", instant, var6);
        }

        return NetworkEncryptionUtils.SignatureData.NONE;
    }

    private static void updateSig(Signature signature, long l, UUID uUID, Instant instant, String string) throws SignatureException {
        signature.update(Longs.toByteArray(l));
        signature.update(uuidToBytes(uUID.getMostSignificantBits(), uUID.getLeastSignificantBits()));
        signature.update(Longs.toByteArray(instant.getEpochSecond()));
        signature.update(string.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] uuidToBytes(long l, long m) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putLong(l).putLong(m);
        return byteBuffer.array();
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
