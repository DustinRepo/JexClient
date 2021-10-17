package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Color coded signs. Use & for the color code")
public class ColoredSigns extends Feature {

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getMode() != EventPacketSent.Mode.PRE)
            return;
        if (eventPacketSent.getPacket() instanceof UpdateSignC2SPacket updateSignC2SPacket) {
            for (int i = 0; i < updateSignC2SPacket.getText().length; i++) {
                updateSignC2SPacket.getText()[i] = updateSignC2SPacket.getText()[i].replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1");
            }
        }
    }

}
