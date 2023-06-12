package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class SpeedMine extends Feature {
    
    private final StopWatch stopWatch = new StopWatch();

    public Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.PROGRESS)
            .build();
    public Property<Integer> delayProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Delay (ms)")
            .value(1)
            .min(0)
            .max(1000)
            .inc(10)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.INSTANT)
            .build();
    public final Property<Boolean> startpProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("StartPacket")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.INSTANT)
            .build();
    public final Property<Boolean> stoppProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("StopPacket")
            .value(true)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.INSTANT)
            .build();
    public Property<Integer> hasteLevelProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Haste Level")
            .value(1)
            .min(1)
            .max(5)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.HASTE)
            .build();
    public Property<Float> breakProgressProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Break Progress")
            .value(0.65f)
            .min(0f)
            .max(1f)
            .inc(0.01f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.PROGRESS)
            .build();
    public Property<Integer> breakCooldownProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Break Cooldown")
            .value(1)
            .min(0)
            .max(5)
            .inc(1)
            .build();

    private boolean givenHaste;

    public SpeedMine() {
        super(Category.PLAYER);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        switch (modeProperty.value()) {
            case PROGRESS, INSTANT -> {
                if (givenHaste && Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE))
                    Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
                float bProgress = modeProperty.value() == Mode.PROGRESS ? breakProgressProperty.value() : 0;
                if (Wrapper.INSTANCE.getIClientPlayerInteractionManager().getBlockBreakProgress() >= bProgress) {
                    Wrapper.INSTANCE.getIClientPlayerInteractionManager().setBlockBreakProgress(1);
                }
                givenHaste = false;
            }
            case HASTE -> {
                givenHaste = true;
                if (Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE) && Wrapper.INSTANCE.getLocalPlayer().getStatusEffect(StatusEffects.HASTE).getAmplifier() > hasteLevelProperty.value() - 1)
                    Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
                if (event.getMode() == EventPlayerPackets.Mode.PRE)
                    Wrapper.INSTANCE.getLocalPlayer().addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 5200, hasteLevelProperty.value() - 1));
            }
        }
        if (Wrapper.INSTANCE.getIClientPlayerInteractionManager().getBlockBreakingCooldown() > breakCooldownProperty.value())
            Wrapper.INSTANCE.getIClientPlayerInteractionManager().setBlockBreakingCooldown(breakCooldownProperty.value());
        this.setSuffix(modeProperty.value());
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        if (modeProperty.value() != Mode.INSTANT)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().isCreative())
            return;
        if (!WorldHelper.INSTANCE.isBreakable(WorldHelper.INSTANCE.getBlock(event.getBlockPos())))
            return;
        if (stopWatch.hasPassed(delayProperty.value())) {
         if (startpProperty.value()) {    
NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, event.getBlockPos(), event.getFace()));
         }
             if(stoppProperty.value()) {
NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, event.getBlockPos(), event.getFace()));
         }
}
        Wrapper.INSTANCE.getIClientPlayerInteractionManager().setBlockBreakProgress(1);
    }, new ClickBlockFilter(EventClickBlock.Mode.PRE));

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
        super.onDisable();
    }

    public enum Mode {
        PROGRESS, INSTANT, HASTE
    }
}
