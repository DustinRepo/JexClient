package me.dustin.jex.helper.misc;

import com.mojang.brigadier.ParseResults;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.load.impl.IClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.message.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

import java.time.Instant;
import java.util.Random;

public enum ChatHelper {
    INSTANCE;

    public void sendChatMessage(String chat) {
        LastSeenMessageList.Acknowledgment acknowledgment = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().consumeAcknowledgment();
        MessageMetadata chatMessageSigner = MessageMetadata.of(Wrapper.INSTANCE.getLocalPlayer().getUuid());
        DecoratedContents decoratedContents = getDecoratedContents(chat, null);
        MessageSignatureData chatSigData = signChatMessage(chatMessageSigner, decoratedContents, acknowledgment.lastSeen());
        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(chat, Instant.now(), new Random().nextLong(), chatSigData, false, acknowledgment));
    }

    public void sendCommand(String command) {
        MessageMetadata chatMessageSigner = MessageMetadata.of(Wrapper.INSTANCE.getLocalPlayer().getUuid());
        ParseResults<CommandSource> parseResults = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getCommandDispatcher().parse(command, Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getCommandSource());
        IClientPlayerEntity iClientPlayerEntity = (IClientPlayerEntity)Wrapper.INSTANCE.getLocalPlayer();
        LastSeenMessageList.Acknowledgment acknowledgment = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().consumeAcknowledgment();
        ArgumentSignatureDataMap argumentSignatureDataMap = iClientPlayerEntity.callSignArguments(chatMessageSigner, parseResults, null, acknowledgment.lastSeen());
        NetworkHelper.INSTANCE.sendPacket(new CommandExecutionC2SPacket(command, chatMessageSigner.timestamp(), new Random().nextLong(), argumentSignatureDataMap, false, acknowledgment));
    }

    public MessageSignatureData signChatMessage(MessageMetadata signer, DecoratedContents decoratedContents, LastSeenMessageList lastSeenMessageList) {
        try {
            Signer lv = Wrapper.INSTANCE.getMinecraft().getProfileKeys().getSigner();
            if (lv != null) {
                return Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getMessagePacker().pack(lv, signer, decoratedContents, lastSeenMessageList).signature();
            }
        } catch (Exception var4) {
            JexClient.INSTANCE.getLogger().error("Failed to sign chat message: '{}'", decoratedContents.decorated().getString(), var4);
        }
        return MessageSignatureData.EMPTY;
    }

    public DecoratedContents getDecoratedContents(String string, Text text) {
        String string2 = StringHelper.truncateChat(string);
        return text != null ? new DecoratedContents(string2, text) : new DecoratedContents(string2);
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
