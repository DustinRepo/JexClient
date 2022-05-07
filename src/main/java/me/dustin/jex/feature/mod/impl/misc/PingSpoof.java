package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;

@Feature.Manifest(category = Feature.Category.MISC, description = "Spoofs your ping to be as high as possible")
public class PingSpoof extends Feature {

    @Op(name = "Ping", min = 1000, max = 14500, inc = 100)
    public int ping = 5000;

    private final StopWatch packetStopWatch = new StopWatch();
    private long keepAliveId = -1;

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            packetStopWatch.reset();
            keepAliveId = -1;
        } else if (keepAliveId != -1 && packetStopWatch.hasPassed(ping)) {
            NetworkHelper.INSTANCE.sendPacketDirect(new ServerboundKeepAlivePacket(keepAliveId));
            keepAliveId = -1;
            packetStopWatch.reset();
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        keepAliveId = ((ServerboundKeepAlivePacket)event.getPacket()).getId();
        packetStopWatch.reset();
        event.cancel();
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundKeepAlivePacket.class));

}
