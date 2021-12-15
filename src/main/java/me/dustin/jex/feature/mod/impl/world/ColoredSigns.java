package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Color coded signs. Use & for the color code")
public class ColoredSigns extends Feature {
    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        UpdateSignC2SPacket updateSignC2SPacket = (UpdateSignC2SPacket) event.getPacket();
        for (int i = 0; i < updateSignC2SPacket.getText().length; i++) {
            updateSignC2SPacket.getText()[i] = updateSignC2SPacket.getText()[i].replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1");
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, UpdateSignC2SPacket.class));
}
