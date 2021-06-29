package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;

@Feature.Manifest(name = "CoordFinder", category = Feature.Category.MISC, description = "Tells you exact coordinates of Wither Spawns and End Portal Activations on vanilla servers.")
public class CoordFinder extends Feature {

    @EventListener(events = {EventPacketReceive.class})
    private void run(Event event) {
        if (event.equals(EventPacketReceive.class)) {
            EventPacketReceive eventPacketReceive = (EventPacketReceive) event;
            if (eventPacketReceive.getPacket() instanceof WorldEventS2CPacket worldEventS2CPacket) {
                if (worldEventS2CPacket.isGlobal()) {
                    switch (worldEventS2CPacket.getEventId()) {
                        case 1023 -> //Wither Boss
                                ChatHelper.INSTANCE.addClientMessage("Wither spawned at: " + worldEventS2CPacket.getPos().toShortString());
                        case 1038 ->//End Portal
                                ChatHelper.INSTANCE.addClientMessage("End Portal Activated at: " + worldEventS2CPacket.getPos().toShortString());
                        case 1028 ->//Ender Dragon
                                ChatHelper.INSTANCE.addClientMessage("Ender Dragon killed at: " + worldEventS2CPacket.getPos().toShortString());
                        default -> ChatHelper.INSTANCE.addClientMessage("Unknown global event at: " + worldEventS2CPacket.getPos().toShortString());
                    }
                }
            }
        }
    }

}
