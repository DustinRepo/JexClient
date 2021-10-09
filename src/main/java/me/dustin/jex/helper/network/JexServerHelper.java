package me.dustin.jex.helper.network;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public enum JexServerHelper {
    INSTANCE;

    private boolean sentConnection = false;

    @EventListener(events = {EventPacketReceive.class})
    private void run(Event event) {
        if (event instanceof EventPacketReceive eventPacketReceive) {
            if (eventPacketReceive.getMode() != EventPacketReceive.Mode.PRE)
                return;
            if (eventPacketReceive.getPacket() instanceof CustomPayloadS2CPacket customPayloadS2CPacket) {
                if (customPayloadS2CPacket.getChannel().getNamespace().equalsIgnoreCase("jex")) {
                    if (customPayloadS2CPacket.getChannel().getPath().equalsIgnoreCase("join_packet")) {
                        if (customPayloadS2CPacket.getData().readString().equalsIgnoreCase("Jex Server Packet")) {
                            EventAPI.getInstance().register(new PayloadSendingHelper());
                        }
                    }
                }
            }
        }
    }

    public static class PayloadSendingHelper {

        @EventListener(events = {EventPlayerPackets.class})
        private void runMethod(EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.POST) {
                sendConnectionPayload();
                while (EventAPI.getInstance().alreadyRegistered(this))
                    EventAPI.getInstance().unregister(this);
            }
        }
        public void sendConnectionPayload() {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            packetByteBuf.writeString("jexversion:" + JexClient.INSTANCE.getVersion());
            NetworkHelper.INSTANCE.sendPacket(new CustomPayloadC2SPacket(new Identifier("jex", "connect"), packetByteBuf));
        }
    }
}
