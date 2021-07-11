package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@Feature.Manifest(name = "WorldTime", category = Feature.Category.WORLD, description = "Change the World time")
public class WorldTime extends Feature {

    @Op(name = "Time", max = 24000)
    public int time = 6000;

    @EventListener(events = {EventPlayerPackets.class, EventPacketReceive.class})
    public void run(Event event) {
        if (event instanceof EventPlayerPackets)
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
                Wrapper.INSTANCE.getWorld().setTimeOfDay(time);
            }

        if (event instanceof EventPacketReceive) {
            if (((EventPacketReceive) event).getPacket() instanceof WorldTimeUpdateS2CPacket)
                event.cancel();
        }

    }
}
