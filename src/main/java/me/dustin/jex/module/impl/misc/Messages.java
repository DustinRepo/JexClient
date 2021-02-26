package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.util.Random;

@ModClass(name = "Messages", category = ModCategory.MISC, description = "Modify messages you send in chat")
public class Messages extends Module {

    @Op(name = "Mode", all = {"Upside-Down", "Backwards", "Random Capital"})
    public String mode = "Upside-Down";

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getPacket() instanceof ChatMessageC2SPacket) {
            switch (mode) {
                case "Upside-Down":
                    eventPacketSent.setPacket(new ChatMessageC2SPacket(upsideDown(((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage())));
                    break;
                case "Backwards":
                    eventPacketSent.setPacket(new ChatMessageC2SPacket(new StringBuilder(((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage()).reverse().toString()));
                    break;
                case "Random Capital":
                    eventPacketSent.setPacket(new ChatMessageC2SPacket(randomCapitalize(((ChatMessageC2SPacket) eventPacketSent.getPacket()).getChatMessage())));
            }
        }
    }

    public String randomCapitalize(String str) {
        String newString = "";
        for (int i = 0; i < str.length(); i++) {
            newString += (new Random().nextBoolean() ? String.valueOf(str.charAt(i)).toUpperCase() : String.valueOf(str.charAt(i)).toLowerCase());
        }
        return newString;
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
