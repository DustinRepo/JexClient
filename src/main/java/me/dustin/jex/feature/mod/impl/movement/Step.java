package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.misc.EventRenderTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventStep;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.Timer;
import java.util.Random;

public class Step extends Feature {

    public final Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.VANILLA)
            .build();
    public final Property<Float> stepHeightProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Step Height")
            .value(1f)
            .min(0.6f)
            .max(10)
            .inc(0.1f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.VANILLA)
            .build();
    public final Property<Boolean> cancelPacketProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Cancel Packet")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.PACKET)
            .build();

    private int cancelPackets;
    private int stepsTillCancel = 2;
    private boolean slow;

    public Step() {
        super(Category.MOVEMENT, "Step up blocks");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        BaritoneHelper.INSTANCE.setAssumeStep(true);
        if (modeProperty.value() == Mode.VANILLA)
            Wrapper.INSTANCE.getPlayer().stepHeight = stepHeightProperty.value();
        else
            Wrapper.INSTANCE.getPlayer().stepHeight = 1.75f;
        this.setSuffix(modeProperty.value());
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventStep> eventStepEventListener = new EventListener<>(event -> {
        if (modeProperty.value() == Mode.PACKET) {
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
        if (cancelPacketProperty.value() && cancelPackets > 0) {
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

    public enum Mode {
        VANILLA, PACKET
    }
}
