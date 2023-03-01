package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.Packet;
import me.dustin.jex.feature.mod.core.Feature;
import java.util.ArrayList;

public class Fakelag extends Feature {

    public final Property<CatchWhen> catchWhenProperty = new Property.PropertyBuilder<CatchWhen>(this.getClass())
            .name("Catch When")
            .value(CatchWhen.BOTH)
            .build();
public final Property<Integer> chokeProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Choke MS")
            .value(100)
            .min(20)
            .max(2000)
            .inc(20)
            .build();

    private final ArrayList<Packet<?>> packets = new ArrayList<>();
    private final StopWatch stopWatch = new StopWatch();
    private boolean sending = false;
    
    public Fakelag() {
        super(Category.MISC, "Pretend to lag");
    }

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (sending)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            packets.clear();
            stopWatch.reset();
        }
        if (!stopWatch.hasPassed(chokeProperty.value()) && shouldCatchPackets()) {
            packets.add(event.getPacket());
            event.cancel();
        } else {
            sending = true;
            packets.forEach(Wrapper.INSTANCE.getLocalPlayer().networkHandler::sendPacket);
            packets.clear();
            stopWatch.reset();
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE));

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            packets.clear();
            stopWatch.reset();
        }
    }, new TickFilter(EventTick.Mode.PRE));

    private boolean shouldCatchPackets() {
        return switch (catchWhenProperty.value()) {
            case BOTH -> true;
            case ON_GROUND -> Wrapper.INSTANCE.getLocalPlayer().isOnGround();
            case IN_AIR -> !Wrapper.INSTANCE.getLocalPlayer().isOnGround();
        };
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            try {
                packets.forEach(Wrapper.INSTANCE.getLocalPlayer().networkHandler::sendPacket);
            } catch (Exception e) {
                return;
            }
            packets.clear();
        }
        super.onDisable();
    }

    public enum CatchWhen {
        BOTH, ON_GROUND, IN_AIR
    }
}
