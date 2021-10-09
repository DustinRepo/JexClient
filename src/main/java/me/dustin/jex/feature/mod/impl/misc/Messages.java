package me.dustin.jex.feature.mod.impl.misc;

import com.google.common.collect.Maps;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Feature.Manifest(name = "Messages", category = Feature.Category.MISC, description = "Modify messages you send in chat")
public class Messages extends Feature {

    @Op(name = "Mode", all = {"Fancy", "Upside-Down", "Backwards", "Random Capital"})
    public String mode = "Fancy";

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getMode() != EventPacketSent.Mode.PRE)
            return;
        if (eventPacketSent.getPacket() instanceof ChatMessageC2SPacket) {
            String message = ((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage();
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
                    eventPacketSent.setPacket(new ChatMessageC2SPacket(s));
                }
                case "Upside-Down" -> eventPacketSent.setPacket(new ChatMessageC2SPacket(upsideDown(message)));
                case "Backwards" -> eventPacketSent.setPacket(new ChatMessageC2SPacket(new StringBuilder(message).reverse().toString()));
                case "Random Capital" -> eventPacketSent.setPacket(new ChatMessageC2SPacket(randomCapitalize(message)));
            }
        }
    }

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
