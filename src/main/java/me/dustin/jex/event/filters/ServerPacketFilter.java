package me.dustin.jex.event.filters;

import me.dustin.jex.event.packet.EventPacketReceive;
import net.minecraft.network.Packet;

import java.util.function.Predicate;

public class ServerPacketFilter implements Predicate<EventPacketReceive> {

    private EventPacketReceive.Mode mode;
    private final Class<? extends Packet<?>>[] packets;

    @SafeVarargs
    public ServerPacketFilter(EventPacketReceive.Mode mode, Class<? extends Packet<?>>... packets) {
        this.mode = mode;
        this.packets = packets;
    }

    @Override
    public boolean test(EventPacketReceive eventPacketReceive) {
        if (packets.length <= 0) {
            if (mode != null)
                return eventPacketReceive.getMode() == mode;
            return true;
        }
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
}
