package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

@Feature.Manifest(category = Feature.Category.MISC, description = "Randomly look around for other players")
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

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Random random = new Random();
        switch (mode) {
            case "Random" -> {
                event.setYaw(random.nextFloat() * 180);
                if (random.nextBoolean())
                    event.setYaw(-event.getYaw());
                event.setPitch(random.nextFloat() * 90);
                if (random.nextBoolean())
                    event.setPitch(-event.getPitch());
            }
            case "Pitch Roll" -> {
                pitch++;
                event.setPitch(pitch);
                if (pitch > 90)
                    pitch = -90;
            }
            case "Yaw Roll" -> {
                yaw++;
                event.setYaw(yaw);
            }
            case "Both Roll" -> {
                pitch++;
                yaw++;
                event.setYaw(yaw);
                event.setPitch(pitch);
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
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
