package me.dustin.jex.helper.misc;

import com.mojang.brigadier.ParseResults;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.load.impl.IClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.network.encryption.ArgumentSignatureDataMap;
import net.minecraft.network.encryption.ChatMessageSignature;
import net.minecraft.network.encryption.ChatMessageSigner;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum ChatHelper {
    INSTANCE;

    public void sendChatMessage(String chat) {
        ChatMessageSigner chatMessageSigner = ChatMessageSigner.create(Wrapper.INSTANCE.getLocalPlayer().getUuid());
        ChatMessageSignature chatSigData = signChatMessage(chatMessageSigner, Text.literal(chat));
        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(chat, chatSigData, false));
    }

    public void sendCommand(String command) {
        ChatMessageSigner chatMessageSigner = ChatMessageSigner.create(Wrapper.INSTANCE.getLocalPlayer().getUuid());
        ParseResults<CommandSource> parseResults = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getCommandDispatcher().parse(command, Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getCommandSource());
        IClientPlayerEntity iClientPlayerEntity = (IClientPlayerEntity)Wrapper.INSTANCE.getLocalPlayer();
        ArgumentSignatureDataMap argumentSignatureDataMap = iClientPlayerEntity.callSignArguments(chatMessageSigner, parseResults, null);
        NetworkHelper.INSTANCE.sendPacket(new CommandExecutionC2SPacket(command, chatMessageSigner.timeStamp(), argumentSignatureDataMap, false));
    }

    private ChatMessageSignature signChatMessage(ChatMessageSigner signer, Text message) {
        try {
            Signer lv = Wrapper.INSTANCE.getMinecraft().getProfileKeys().getSigner();
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
