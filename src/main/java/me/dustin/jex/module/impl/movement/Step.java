package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventStep;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Random;

@ModClass(name = "Step", category = ModCategory.MOVEMENT, description = "Step up blocks")
public class Step extends Module {

    @Op(name = "Mode", all = {"Vanilla", "Packet"})
    public String mode = "Vanilla";
    @OpChild(name = "Step Height", min = 0.6f, max = 10f, inc = 0.1f, parent = "Mode", dependency = "Vanilla")
    public float stepHeight = 1;
    @OpChild(name = "Cancel Packet", parent = "Mode", dependency = "Packet")
    public boolean cancelPacket = true;

    private int cancelPackets;
    private int stepsTillCancel = 2;
    @EventListener(events = {EventPlayerPackets.class, EventStep.class, EventPacketSent.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets) {
            EventPlayerPackets eventPlayerPackets = (EventPlayerPackets)event;
            BaritoneHelper.INSTANCE.setAssumeStep(true);
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (mode.equalsIgnoreCase("Vanilla"))
                    Wrapper.INSTANCE.getLocalPlayer().stepHeight = stepHeight;
                else
                    Wrapper.INSTANCE.getLocalPlayer().stepHeight = 1.75f;
            }
            this.setSuffix(mode);
        } else if (event instanceof EventStep && mode.equalsIgnoreCase("Packet")) {
            EventStep eventStep = (EventStep)event;
            switch (eventStep.getMode()) {
                case PRE:
                    Wrapper.INSTANCE.getIRenderTickCounter().setTimeScale(1000f / (20f * 0.3f));
                    break;
                case MID:
                    NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.42399999499321, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                    NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + (eventStep.getStepHeight() > 1f ? 0.76111999664784 : 0.75), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                    if (eventStep.getStepHeight() > 1f) {
                            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 1.01309760317355, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                            NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 1.18163566084895, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                    }
                    if (eventStep.getStepHeight() > 1.3f) {
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 1.26840295905959, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                        NetworkHelper.INSTANCE.sendPacket(new PlayerMoveC2SPacket.PositionOnly(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 1.20313422336366, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
                    }
                break;
                case END:
                    Wrapper.INSTANCE.getIRenderTickCounter().setTimeScale(1000f / 20f);
                        if (stepsTillCancel == 0) {
                            cancelPackets = 1;
                            stepsTillCancel = new Random().nextBoolean() ? 2 : 1;
                            if (eventStep.getStepHeight() > 1.3f)
                                stepsTillCancel = 1;
                        } else {
                            stepsTillCancel--;
                        }
                    break;
                case POST:
                    Wrapper.INSTANCE.getIRenderTickCounter().setTimeScale(1000f / 20f);
                    break;
            }
        } else if (event instanceof EventPacketSent) {
            EventPacketSent eventPacketSent = (EventPacketSent)event;
            if (eventPacketSent.getPacket() instanceof PlayerMoveC2SPacket && cancelPackets > 0 && cancelPacket)
            {
                PlayerMoveC2SPacket playerMoveC2SPacket = (PlayerMoveC2SPacket)eventPacketSent.getPacket();
                double yDif = playerMoveC2SPacket.getY(Wrapper.INSTANCE.getLocalPlayer().getY()) - Wrapper.INSTANCE.getLocalPlayer().getY();
                if (!(yDif == 0.42D || yDif == 0.75D || yDif == 1)) {
                    eventPacketSent.cancel();
                    cancelPackets--;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        BaritoneHelper.INSTANCE.setAssumeStep(false);
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().stepHeight = 0.6f;
        super.onDisable();
    }
}
