package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.util.Hand;

@Feature.Manifest(name = "AntiAFK", category = Feature.Category.MISC, description = "Prevent yourself from being detected as AFK and potentially kicked")
public class AntiAFK extends Feature {

    @Op(name = "Mode", all = {"Swing", "Jump", "Chat"})
    public String mode = "Swing";

    @Op(name = "Timer (Seconds)", min = 5, max = 120, inc = 1)
    public int secondsDelay = 5;

    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (timer.hasPassed(secondsDelay * 1000)) {
                switch (mode) {
                    case "Swing":
                        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                        break;
                    case "Jump":
                        if (Wrapper.INSTANCE.getLocalPlayer().isOnGround())
                            Wrapper.INSTANCE.getLocalPlayer().jump();
                        break;
                    case "Walk":
                        NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(Wrapper.INSTANCE.getLocalPlayer().age + ""));
                        break;
                }
                timer.reset();
            }
        }
    }

}
