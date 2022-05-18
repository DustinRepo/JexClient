package me.dustin.jex.helper.misc;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.class_7501;
import net.minecraft.network.encryption.ChatMessageSignature;
import net.minecraft.network.encryption.ChatMessageSigner;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum ChatHelper {
    INSTANCE;

    public void sendChatMessage(String chat) {
        ChatMessageSigner chatMessageSigner = ChatMessageSigner.create(Wrapper.INSTANCE.getLocalPlayer().getUuid());
        ChatMessageSignature chatSigData = signChatMessage(chatMessageSigner, Text.literal(chat));
        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(chat, chatSigData, false));
    }

    private ChatMessageSignature signChatMessage(ChatMessageSigner signer, Text message) {
        try {
            class_7501 lv = Wrapper.INSTANCE.getMinecraft().getProfileKeys().method_44287();
            if (lv != null) {
                return signer.sign(lv, message);
            }
        } catch (Exception var4) {
            JexClient.INSTANCE.getLogger().error("Failed to sign chat message: '{}'", message.getString(), var4);
        }

        return ChatMessageSignature.none();
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
