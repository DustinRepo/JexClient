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
    
       public static class Receive extends PacketEvent {
        private static final Receive INSTANCE = new Receive();

        public static Receive get(Packet<?> packet) {
            INSTANCE.packet = packet;
            return INSTANCE;
        }
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
    
}
