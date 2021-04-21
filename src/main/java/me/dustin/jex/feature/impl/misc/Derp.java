package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

@Feat(name = "Derp", category = FeatureCategory.MISC, description = "Randomly look around for other players")
public class Derp extends Feature {

    @Op(name = "Swing")
    public boolean swing = true;
    @OpChild(name = "Show Swing", parent = "Swing")
    public boolean showSwing = true;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        Random random = new Random();
        eventPlayerPackets.setYaw(random.nextFloat() * 180);
        if (random.nextBoolean())
            eventPlayerPackets.setYaw(-eventPlayerPackets.getYaw());
        eventPlayerPackets.setPitch(random.nextFloat() * 90);
        if (random.nextBoolean())
            eventPlayerPackets.setPitch(-eventPlayerPackets.getPitch());
        if (swing) {
            Hand hand = random.nextBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
            if (showSwing) {
                Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
            } else {
                NetworkHelper.INSTANCE.sendPacket(new HandSwingC2SPacket(hand));
            }
        }
    }

}
