package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventRenderTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventStep;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.Timer;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.util.Random;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Step up blocks")
public class Step extends Feature {

    @Op(name = "Mode", all = {"Vanilla", "Packet"})
    public String mode = "Vanilla";
    @OpChild(name = "Step Height", min = 0.6f, max = 10f, inc = 0.1f, parent = "Mode", dependency = "Vanilla")
    public float stepHeight = 1;
    @OpChild(name = "Cancel Packet", parent = "Mode", dependency = "Packet")
    public boolean cancelPacket = true;

    private int cancelPackets;
    private int stepsTillCancel = 2;
    private boolean slow;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        BaritoneHelper.INSTANCE.setAssumeStep(true);
        if (mode.equalsIgnoreCase("Vanilla"))
            Wrapper.INSTANCE.getPlayer().stepHeight = stepHeight;
        else
            Wrapper.INSTANCE.getPlayer().stepHeight = 1.75f;
        this.setSuffix(mode);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventStep> eventStepEventListener = new EventListener<>(event -> {
        if (mode.equalsIgnoreCase("Packet")) {
            switch (event.getMode()) {
                case PRE -> slow = true;
                case MID -> {
                    NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY() + 0.42399999499321, Wrapper.INSTANCE.getPlayer().getZ(), false));
                    NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY() + (event.getStepHeight() > 1f ? 0.76111999664784 : 0.75), Wrapper.INSTANCE.getPlayer().getZ(), false));
                    if (event.getStepHeight() > 1f) {
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY() + 1.01309760317355, Wrapper.INSTANCE.getPlayer().getZ(), false));
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY() + 1.18163566084895, Wrapper.INSTANCE.getPlayer().getZ(), false));
                    }
                    if (event.getStepHeight() > 1.3f) {
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY() + 1.26840295905959, Wrapper.INSTANCE.getPlayer().getZ(), false));
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getPlayer().getX(), Wrapper.INSTANCE.getPlayer().getY() + 1.20313422336366, Wrapper.INSTANCE.getPlayer().getZ(), false));
                    }
                }
                case END -> {
                    slow = false;
                    if (stepsTillCancel == 0) {
                        cancelPackets = 1;
                        stepsTillCancel = new Random().nextBoolean() ? 2 : 1;
                        if (event.getStepHeight() > 1.3f)
                            stepsTillCancel = 1;
                    } else {
                        stepsTillCancel--;
                    }
                }
                case POST -> slow = false;
            }
        }
    });

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        PlayerMoveC2SPacket playerMoveC2SPacket = (PlayerMoveC2SPacket) event.getPacket();
        if (cancelPacket && cancelPackets > 0) {
            double yDif = playerMoveC2SPacket.getY(Wrapper.INSTANCE.getPlayer().getY()) - Wrapper.INSTANCE.getPlayer().getY();
            if (!(yDif == 0.42D || yDif == 0.75D || yDif == 1)) {
                event.cancel();
                cancelPackets--;
            }
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, PlayerMoveC2SPacket.class));

    @EventPointer
    private final EventListener<EventRenderTick> eventRenderTickEventListener = new EventListener<>(event -> {
        if (slow) {
            event.timeScale = 1000f / (20f * 0.3f);
        } else if (!Feature.getState(Timer.class))
           event.timeScale = 1000.f / 20.f;
    });

    @Override
    public void onDisable() {
        BaritoneHelper.INSTANCE.setAssumeStep(false);
        if (Wrapper.INSTANCE.getPlayer() != null)
            Wrapper.INSTANCE.getPlayer().stepHeight = 0.6f;
        super.onDisable();
    }
}
