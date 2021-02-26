package me.dustin.jex.module.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@ModClass(name = "WorldTime", category = ModCategory.WORLD, description = "Change the World time")
public class WorldTime extends Module {

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
