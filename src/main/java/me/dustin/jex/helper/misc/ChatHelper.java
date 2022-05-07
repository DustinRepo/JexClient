package me.dustin.jex.helper.misc;

import com.google.common.primitives.Longs;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.util.Crypt;
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
        MessageSignature chatSigData = new MessageSignature(Wrapper.INSTANCE.getLocalPlayer().getUUID(), instant, sigForMessage(instant, chat));
        NetworkHelper.INSTANCE.sendPacket(new ServerboundChatPacket(chat, chatSigData));
    }

    private Crypt.SaltSignaturePair sigForMessage(Instant instant, String string) {
        try {
            Signature signature = Wrapper.INSTANCE.getMinecraft().getProfileKeyPairManager().createSignature();
            if (signature != null) {
                long l = Crypt.SaltSupplier.getLong();
                updateSig(signature, l, Wrapper.INSTANCE.getLocalPlayer().getUUID(), instant, string);
                return new Crypt.SaltSignaturePair(l, signature.sign());
            }
        } catch (GeneralSecurityException var6) {
            JexClient.INSTANCE.getLogger().error("Failed to sign chat message {}", instant, var6);
        }

        return Crypt.SaltSignaturePair.EMPTY;
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
        Wrapper.INSTANCE.getMinecraft().gui.getChat().addMessage(Component.nullToEmpty(String.format("%s[%sJex%s]%s: %s%s", ChatFormatting.DARK_GRAY, ChatFormatting.AQUA, ChatFormatting.DARK_GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY, message)));
    }

    public void addRawMessage(String message) {
        Wrapper.INSTANCE.getMinecraft().gui.getChat().addMessage(Component.nullToEmpty(message));
    }

    public void addRawMessage(Component message) {
        Wrapper.INSTANCE.getMinecraft().gui.getChat().addMessage(message);
    }
}
