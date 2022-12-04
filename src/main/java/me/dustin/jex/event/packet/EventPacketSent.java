package me.dustin.jex.event.packet;

import me.dustin.events.core.Event;
import net.minecraft.network.Packet;

public class EventPacketSent extends Event {

    private Packet<?> packet;
    private final Mode mode;

    public EventPacketSent(Packet<?> packet, Mode mode) {
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

    public static class EventPacketSentDirect extends Event {

        private Packet<?> packet;
        private EventPacketSent.Mode mode;

        public EventPacketSentDirect(Packet<?> packet, EventPacketSent.Mode mode) {
            this.packet = packet;
            this.mode = mode;
        }
        
        public static class Sent extends EventPacketSent {
        private static final Sent INSTANCE = new Sent();

        public static Sent get(Packet<?> packet) {
            INSTANCE.setCancelled(false);
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }
        
        public static class Send extends EventPacketSent {
        private static final Send INSTANCE = new Send();

        public static Send get(Packet<?> packet) {
            INSTANCE.setCancelled(false);
            INSTANCE.packet = packet;
            return INSTANCE;
        }
    }  
        
        public Packet<?> getPacket() {
            return packet;
        }

        public EventPacketSent.Mode getMode() {
            return mode;
        }

        public void setPacket(Packet<?> packet) {
            this.packet = packet;
        }

        public enum Mode {
            PRE, POST
        }
    }
}
