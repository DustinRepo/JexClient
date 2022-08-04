package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.message.DecoratedContents;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageMetadata;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.Random;

public class Messages extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.FANCY)
            .build();

    public Messages() {
        super(Category.MISC, "Modify messages you send in chat");
    }

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        ChatMessageC2SPacket chatMessageC2SPacket = (ChatMessageC2SPacket) event.getPacket();
        String message = chatMessageC2SPacket.chatMessage();
        if (message.startsWith("/"))
            return;
        switch (modeProperty.value()) {
            case FANCY -> {
                String fancyChars = "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ１２３４５６７８９０－＝｀～！＠＃＄％＾＆＊＼，＜．＞／？：；＇＂";
                String replaceChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-=`~!@#$%^&*\\,<.>/?:;'\"";
                String s = message;
                for (int i = 0; i < fancyChars.length(); i++) {
                    char currentChar = replaceChars.charAt(i);
                    char replace = fancyChars.charAt(replaceChars.indexOf(currentChar));
                    s = s.replace(currentChar, replace);
                }
                message = s;
            }
            case UPSIDE_DOWN -> message = upsideDown(message);
            case BACKWARDS -> message = new StringBuilder(message).reverse().toString();
            case RANDOM_CAPITAL -> message = randomCapitalize(message);
        }
        DecoratedContents decoratedContents = ChatHelper.INSTANCE.getDecoratedContents(message, null);
        LastSeenMessageList.Acknowledgment acknowledgment = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().consumeAcknowledgment();
        MessageMetadata chatMessageSigner = MessageMetadata.of(Wrapper.INSTANCE.getLocalPlayer().getUuid());
        MessageSignatureData chatSigData = ChatHelper.INSTANCE.signChatMessage(chatMessageSigner, decoratedContents, acknowledgment.lastSeen());
        NetworkHelper.INSTANCE.sendPacketDirect(new ChatMessageC2SPacket(message, Instant.now(), new Random().nextLong(), chatSigData, false, acknowledgment));
        event.cancel();
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ChatMessageC2SPacket.class));

    public String randomCapitalize(String str) {
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            newString.append(new Random().nextBoolean() ? String.valueOf(str.charAt(i)).toUpperCase() : String.valueOf(str.charAt(i)).toLowerCase());
        }
        return newString.toString();
    }

    public String upsideDown(String str) {
        String normal = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'";
        String split = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,";

        normal += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        split += "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ";

        normal += "0123456789";
        split += "0ƖᄅƐㄣϛ9ㄥ86";

        String newstr = "";
        char letter;
        for (int i = 0; i < str.length(); i++) {
            letter = str.charAt(i);

            int a = normal.indexOf(letter);
            newstr += (a != -1) ? split.charAt(a) : letter;
        }
        return new StringBuilder(newstr).reverse().toString();
    }

    public enum Mode {
        FANCY, UPSIDE_DOWN, BACKWARDS, RANDOM_CAPITAL
    }
}
