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
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class SpeedMine extends Feature {

    public Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.PROGRESS)
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
            .description("How far into breaking a block before automatically finishing.")
            .value(0.65f)
            .max(0.95f)
            .inc(0.05f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.PROGRESS)
            .build();
    public Property<Integer> breakCooldownProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Break Cooldown")
            .description("The amount of ticks to wait between breaking blocks. Default MC is 5.")
            .value(1)
            .max(5)
            .build();

    private boolean givenHaste;

    public SpeedMine() {
        super(Category.PLAYER, "Break blocks faster");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().isCreative())
            return;
        switch (modeProperty.value()) {
            case PROGRESS, INSTANT -> {
                if (givenHaste && Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE))
                    Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
                float bProgress = modeProperty.value() == Mode.PROGRESS ? breakProgressProperty.value() : 0;
                if (!WorldHelper.INSTANCE.isBreakable(WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getIClientPlayerInteractionManager().currentBreakingPos()))) {
                    givenHaste = false;
                    break;
                }
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
        for (int i = 0; i < 10; i++) {
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, event.getBlockPos(), event.getFace()));
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
