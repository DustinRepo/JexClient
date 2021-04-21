package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.Packet;

import java.util.ArrayList;

@Feat(name = "Fakelag", category = FeatureCategory.MISC, description = "Pretend to lag")
public class Fakelag extends Feature {

    @Op(name = "Choke MS", min = 50, max = 2000, inc = 10)
    public int choke = 100;

    private ArrayList<Packet> packets = new ArrayList<>();
    private Timer timer = new Timer();
    private boolean sending = false;

    @EventListener(events = {EventPacketSent.class})
    private void runMethod(EventPacketSent eventPacketSent) {
        if (sending)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            packets.clear();
            timer.reset();
        }
        if (!timer.hasPassed(choke)) {
            packets.add(eventPacketSent.getPacket());
            eventPacketSent.cancel();
        } else {
            sending = true;
            packets.forEach(packet -> Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(packet));
            sending = false;
            packets.clear();
            timer.reset();
        }
    }
}
