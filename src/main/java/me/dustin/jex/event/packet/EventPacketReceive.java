package me.dustin.jex.event.packet;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.network.Packet;

public class EventPacketReceive extends Event {

    private Packet<?> packet;
    private Mode mode;

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
}
