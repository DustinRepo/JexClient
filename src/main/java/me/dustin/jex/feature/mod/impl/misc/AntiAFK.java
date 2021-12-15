package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.util.Hand;

@Feature.Manifest(category = Feature.Category.MISC, description = "Prevent yourself from being detected as AFK and potentially kicked")
public class AntiAFK extends Feature {

    @Op(name = "Mode", all = {"Swing", "Jump", "Chat"})
    public String mode = "Swing";

    @Op(name = "Timer (Seconds)", min = 5, max = 120, inc = 1)
    public int secondsDelay = 5;

    private Timer timer = new Timer();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (timer.hasPassed(secondsDelay * 1000L)) {
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
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
