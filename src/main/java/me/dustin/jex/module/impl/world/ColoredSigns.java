package me.dustin.jex.module.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

@ModClass(name = "ColoredSigns", category = ModCategory.WORLD, description = "Color coded signs. Use & for the color code")
public class ColoredSigns extends Module {

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getPacket() instanceof UpdateSignC2SPacket) {
            UpdateSignC2SPacket updateSignC2SPacket = (UpdateSignC2SPacket) eventPacketSent.getPacket();
            for (int i = 0; i < updateSignC2SPacket.getText().length; i++) {
                updateSignC2SPacket.getText()[i] = updateSignC2SPacket.getText()[i].replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1");
            }
        }
    }

}
