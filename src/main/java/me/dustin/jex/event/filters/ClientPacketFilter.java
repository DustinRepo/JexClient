package me.dustin.jex.event.filters;

import me.dustin.jex.event.packet.EventPacketSent;
import net.minecraft.network.protocol.Packet;
import java.util.function.Predicate;

public class ClientPacketFilter implements Predicate<EventPacketSent> {

    private EventPacketSent.Mode mode;
    private final Class<? extends Packet<?>>[] packets;

    @SafeVarargs
    public ClientPacketFilter(EventPacketSent.Mode mode, Class<? extends Packet<?>>... packets) {
        this.mode = mode;
        this.packets = packets;
    }

    @Override
    public boolean test(EventPacketSent eventPacketReceive) {
        if (packets.length > 0) {
            for (Class<? extends Packet<?>> packetClass : packets) {
                if (packetClass == eventPacketReceive.getPacket().getClass() || packetClass == eventPacketReceive.getPacket().getClass().getSuperclass()) {
                    if (mode != null) {
                        return eventPacketReceive.getMode() == mode;
                    }
                    return true;
                }
            }
            return false;
        }
        if (mode != null)
            return eventPacketReceive.getMode() == mode;
        return true;
    }
}
