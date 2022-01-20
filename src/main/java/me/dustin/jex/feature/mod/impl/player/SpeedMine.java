package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClickBlockFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Break blocks faster")
public class SpeedMine extends Feature {

    @Op(name = "Mode", all = {"Progress", "Instant", "Haste"})
    public String mode = "Progress";
    @Op(name = "Break Cooldown", max = 5)
    public int breakCooldown = 1;
    @OpChild(name = "Haste Level", min = 1, max = 5, parent = "Mode", dependency = "Haste")
    public int haste = 1;
    @OpChild(name = "Break Progress", max = 0.9f, inc = 0.01f, parent = "Mode", dependency = "Progress")
    public float progress = 0.65f;

    private boolean givenHaste;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().isCreative())
            return;
        switch (mode) {
            case "Progress", "Instant" -> {
                if (givenHaste && Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE))
                    Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
                float bProgress = mode.equalsIgnoreCase("Progress") ? progress : 0;
                if (!WorldHelper.INSTANCE.isBreakable(WorldHelper.INSTANCE.getBlock(Wrapper.INSTANCE.getIInteractionManager().currentBreakingPos()))) {
                    givenHaste = false;
                    break;
                }
                if (Wrapper.INSTANCE.getIInteractionManager().getBlockBreakProgress() >= bProgress) {
                    Wrapper.INSTANCE.getIInteractionManager().setBlockBreakProgress(1);
                }
                givenHaste = false;
            }
            case "Haste" -> {
                givenHaste = true;
                if (Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE) && Wrapper.INSTANCE.getLocalPlayer().getStatusEffect(StatusEffects.HASTE).getAmplifier() > haste - 1)
                    Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
                if (event.getMode() == EventPlayerPackets.Mode.PRE)
                    Wrapper.INSTANCE.getLocalPlayer().addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 5200, haste - 1));
            }
        }
        if (Wrapper.INSTANCE.getIInteractionManager().getBlockBreakingCooldown() > breakCooldown)
            Wrapper.INSTANCE.getIInteractionManager().setBlockBreakingCooldown(breakCooldown);
        this.setSuffix(mode);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventClickBlock> eventClickBlockEventListener = new EventListener<>(event -> {
        if (!"Instant".equalsIgnoreCase(mode))
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().isCreative())
            return;
        if (!WorldHelper.INSTANCE.isBreakable(WorldHelper.INSTANCE.getBlock(event.getBlockPos())))
            return;
        for (int i = 0; i < 10; i++) {
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, event.getBlockPos(), event.getFace()));
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, event.getBlockPos(), event.getFace()));
        }
        Wrapper.INSTANCE.getIInteractionManager().setBlockBreakProgress(1);
    }, new ClickBlockFilter(EventClickBlock.Mode.PRE));

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
        super.onDisable();
    }

}
