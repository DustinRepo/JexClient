package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import me.dustin.jex.feature.mod.core.Feature;

public class WorldTime extends Feature {

    public final Property<Long> timeProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Time")
            .value(6000L)
            .max(24000)
            .build();

    public WorldTime() {
        super(Category.WORLD, "Change the World time");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getWorld().setTimeOfDay(timeProperty.value());
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> event.cancel(), new ServerPacketFilter(EventPacketReceive.Mode.PRE, WorldTimeUpdateS2CPacket.class));
}
