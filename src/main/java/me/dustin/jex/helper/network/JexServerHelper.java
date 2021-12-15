package me.dustin.jex.helper.network;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public enum JexServerHelper {
    INSTANCE;

    private boolean sentConnection = false;

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        CustomPayloadS2CPacket customPayloadS2CPacket = (CustomPayloadS2CPacket) event.getPacket();
        if (customPayloadS2CPacket.getChannel().getNamespace().equalsIgnoreCase("jex")) {
            if (customPayloadS2CPacket.getChannel().getPath().equalsIgnoreCase("join_packet")) {
                if (customPayloadS2CPacket.getData().readString().equalsIgnoreCase("Jex Server Packet")) {
                    EventManager.register(new PayloadSendingHelper());
                }
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, CustomPayloadS2CPacket.class));

    public static class PayloadSendingHelper {

        @EventPointer
        private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
            sendConnectionPayload();
            EventManager.unregister(this);
        }, new PlayerPacketsFilter(EventPlayerPackets.Mode.POST));

        public void sendConnectionPayload() {
            PacketByteBuf packetByteBuf = PacketByteBufs.create();
            packetByteBuf.writeString("jexversion:" + JexClient.INSTANCE.getVersion().version());
            NetworkHelper.INSTANCE.sendPacket(new CustomPayloadC2SPacket(new Identifier("jex", "connect"), packetByteBuf));
        }
    }
}
