package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.util.Random;

@Feature.Manifest(name = "Messages", category = Feature.Category.MISC, description = "Modify messages you send in chat")
public class Messages extends Feature {

    @Op(name = "Mode", all = {"Upside-Down", "Backwards", "Random Capital"})
    public String mode = "Upside-Down";

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getPacket() instanceof ChatMessageC2SPacket) {
            if (((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage().startsWith("/"))
                return;
            switch (mode) {
                case "Upside-Down" -> eventPacketSent.setPacket(new ChatMessageC2SPacket(upsideDown(((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage())));
                case "Backwards" -> eventPacketSent.setPacket(new ChatMessageC2SPacket(new StringBuilder(((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage()).reverse().toString()));
                case "Random Capital" -> eventPacketSent.setPacket(new ChatMessageC2SPacket(randomCapitalize(((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage())));
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
