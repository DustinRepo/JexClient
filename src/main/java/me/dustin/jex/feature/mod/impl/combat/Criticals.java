package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Feature {

    public final Property<Boolean> livingOnlyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Living Only")
            .description("Only use Criticals on living entities.")
            .value(true)
            .build();
    public final Property<Float> delayProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Time Delay (S)")
            .description("Delay between send critical packets in seconds")
            .value(0f)
            .min(0f)
            .max(10f)
            .inc(0.1f)
            .build();
    public final Property<Boolean> extraParticlesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Extra Particles")
            .description("Whether or not to add extra (fake) critical particles.")
            .value(true)
            .build();
    public final Property<Integer> amountProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Amount")
            .value(5)
            .min(1)
            .max(20)
            .inc(1)
            .parent(extraParticlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();

    public Criticals() {
        super(Category.COMBAT, "Automatically deal critical strikes when attacking.");
    }
    
    private final StopWatch stopWatch = new StopWatch();
    Float value = delayProperty.value()
    Long delay = value.longValue()*1000;

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (livingOnlyProperty.value() && !(event.getEntity() instanceof LivingEntity))
            return;
        if (extraParticlesProperty.value()) {
            for (int i = 0; i < amountProperty.value(); i++) {
                Wrapper.INSTANCE.getLocalPlayer().addCritParticles(event.getEntity());
            }
        }
        if (Wrapper.INSTANCE.getLocalPlayer().isSprinting()) //mc recently (1.15?) made it so you can't crit while sprinting
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        crit();
    });

    public void crit() {
        if (stopWatch.hasPassed(delay))
        if (Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.05F, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.012511F, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            stopWatch.reset();
        }
    }
}
