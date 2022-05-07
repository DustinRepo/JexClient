package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.network.protocol.Packet;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.MISC, description = "Pretend to lag")
public class Fakelag extends Feature {

    @Op(name = "Catch when", all = {"Both", "OnGround", "In Air"})
    public String catchWhen = "Both";
    @Op(name = "Choke MS", min = 50, max = 2000, inc = 10)
    public int choke = 100;

    private final ArrayList<Packet<?>> packets = new ArrayList<>();
    private final StopWatch stopWatch = new StopWatch();
    private boolean sending = false;

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (sending)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            packets.clear();
            stopWatch.reset();
        }
        if (!stopWatch.hasPassed(choke) && shouldCatchPackets()) {
            packets.add(event.getPacket());
            event.cancel();
        } else {
            sending = true;
            packets.forEach(Wrapper.INSTANCE.getLocalPlayer().connection::send);
            sending = false;
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
        return switch (catchWhen.toLowerCase()) {
            case "both" -> true;
            case "onground" -> Wrapper.INSTANCE.getLocalPlayer().isOnGround();
            case "in air" -> !Wrapper.INSTANCE.getLocalPlayer().isOnGround();
            default -> false;
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
                packets.forEach(Wrapper.INSTANCE.getLocalPlayer().connection::send);
            } catch (Exception e) {}
            packets.clear();
        }
        super.onDisable();
    }
}
