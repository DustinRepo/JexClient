package me.dustin.jex.event.packet;

import me.dustin.events.core.Event;
import net.minecraft.network.Packet;

public class EventPacketReceive extends Event {

    private Packet<?> packet;
    private final Mode mode;

    public EventPacketReceive(Packet<?> packet, Mode mode) {
        this.packet = packet;
        this.mode = mode;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Mode getMode() {
        return mode;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public enum Mode {
        PRE, POST
    }
    
       public static class Sent extends PacketEventReceive {
        private static final Sent INSTANCE = new Sent();

        public static Sent get(Packet<?> packet) {
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }
    
}
