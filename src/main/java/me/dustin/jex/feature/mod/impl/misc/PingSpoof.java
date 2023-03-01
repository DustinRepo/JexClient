package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;

public class PingSpoof extends Feature {

    public final Property<Long> pingProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Ping")
            .value(5000L)
            .min(1000)
            .max(10000)
            .inc(100)
            .build();

    private final StopWatch packetStopWatch = new StopWatch();
    private long keepAliveId = -1;

    public PingSpoof() {
        super(Category.MISC, "Spoofs your ping to be as high as possible");
    }

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            packetStopWatch.reset();
            keepAliveId = -1;
        } else if (keepAliveId != -1 && packetStopWatch.hasPassed(pingProperty.value())) {
            NetworkHelper.INSTANCE.sendPacketDirect(new KeepAliveC2SPacket(keepAliveId));
            keepAliveId = -1;
            packetStopWatch.reset();
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        keepAliveId = ((KeepAliveC2SPacket)event.getPacket()).getId();
        packetStopWatch.reset();
        event.cancel();
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, KeepAliveC2SPacket.class));
    
@Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            try {
                NetworkHelper.INSTANCE.sendPacketDirect(new KeepAliveC2SPacket(keepAliveId));
            } catch (Exception e) {}
              keepAliveId = -1;
        }
        super.onDisable();
    }
}
