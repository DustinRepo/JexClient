package me.dustin.jex.feature.mod.impl.misc;

import io.netty.buffer.Unpooled;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import java.nio.charset.StandardCharsets;

@Feature.Manifest(category = Feature.Category.MISC, description = "Tell the server you are a vanilla player")
public class VanillaSpoof extends Feature {

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        ServerboundCustomPayloadPacket packet = (ServerboundCustomPayloadPacket) event.getPacket();
        if (packet.getIdentifier() == ServerboundCustomPayloadPacket.BRAND) {
            ServerboundCustomPayloadPacket newPacket = new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf("vanilla"));
            event.setPacket(newPacket);
        } else if (packet.getData().toString(StandardCharsets.UTF_8).toLowerCase().contains("fabric")) {
            event.cancel();
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundCustomPayloadPacket.class));
}
