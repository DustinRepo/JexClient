package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.util.Random;

@Feature.Manifest(category = Feature.Category.MISC, description = "Modify messages you send in chat")
public class Messages extends Feature {

    @Op(name = "Mode", all = {"Fancy", "Upside-Down", "Backwards", "Random Capital"})
    public String mode = "Fancy";

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        String message = ((ChatMessageC2SPacket) event.getPacket()).getChatMessage();
        if (message.startsWith("/"))
            return;
        switch (mode) {
            case "Fancy" -> {
                String fancyChars = "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ１２３４５６７８９０－＝｀～！＠＃＄％＾＆＊＼，＜．＞／？：；＇＂";
                String replaceChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-=`~!@#$%^&*\\,<.>/?:;'\"";
                String s = message;
                for (int i = 0; i < fancyChars.length(); i++) {
                    char currentChar = replaceChars.charAt(i);
                    char replace = fancyChars.charAt(replaceChars.indexOf(currentChar));
                    s = s.replace(currentChar, replace);
                }
                event.setPacket(new ChatMessageC2SPacket(s));
            }
            case "Upside-Down" -> event.setPacket(new ChatMessageC2SPacket(upsideDown(message)));
            case "Backwards" -> event.setPacket(new ChatMessageC2SPacket(new StringBuilder(message).reverse().toString()));
            case "Random Capital" -> event.setPacket(new ChatMessageC2SPacket(randomCapitalize(message)));
        }
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
//maj
        normal += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        split += "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ";
//number
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
}
