package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

@Feature.Manifest(name = "Derp", category = Feature.Category.MISC, description = "Randomly look around for other players")
public class Derp extends Feature {

    @Op(name = "Mode", all = {"Random", "Pitch Roll", "Yaw Roll", "Both Roll"})
    public String mode = "Random";
    @Op(name = "Normalize Angles")
    public boolean normalize = false;
    @Op(name = "Swing")
    public boolean swing = true;
    @OpChild(name = "Show Swing", parent = "Swing")
    public boolean showSwing = true;

    private int yaw, pitch;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        Random random = new Random();
        switch (mode) {
            case "Random" -> {
                eventPlayerPackets.setYaw(random.nextFloat() * 180);
                if (random.nextBoolean())
                    eventPlayerPackets.setYaw(-eventPlayerPackets.getYaw());
                eventPlayerPackets.setPitch(random.nextFloat() * 90);
                if (random.nextBoolean())
                    eventPlayerPackets.setPitch(-eventPlayerPackets.getPitch());
            }
            case "Pitch Roll" -> {
                pitch++;
                eventPlayerPackets.setPitch(pitch);
                if (pitch > 90)
                    pitch = -90;
            }
            case "Yaw Roll" -> {
                yaw++;
                eventPlayerPackets.setYaw(yaw);
            }
            case "Both Roll" -> {
                pitch++;
                yaw++;
                eventPlayerPackets.setYaw(yaw);
                eventPlayerPackets.setPitch(pitch);
                if (pitch > 90)
                    pitch = -90;
            }
        }
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
