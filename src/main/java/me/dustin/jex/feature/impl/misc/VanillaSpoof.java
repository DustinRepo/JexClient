package me.dustin.jex.feature.impl.misc;

import io.netty.buffer.Unpooled;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

import java.nio.charset.StandardCharsets;

@Feat(name = "VanillaSpoof", category = FeatureCategory.MISC, description = "Tell the server you are a vanilla player")
public class VanillaSpoof extends Feature {

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (eventPacketSent.getPacket() instanceof CustomPayloadC2SPacket packet) {
            if (packet.getChannel() == CustomPayloadC2SPacket.BRAND) {
                CustomPayloadC2SPacket newPacket = new CustomPayloadC2SPacket(CustomPayloadC2SPacket.BRAND, new PacketByteBuf(Unpooled.buffer()).writeString("vanilla"));
                eventPacketSent.setPacket(newPacket);
            } else if (packet.getData().toString(StandardCharsets.UTF_8).toLowerCase().contains("fabric")) {
                eventPacketSent.cancel();
            }
        }
    }

}
