package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@ModClass(name = "Speedmine", category = ModCategory.PLAYER, description = "Break blocks faster")
public class SpeedMine extends Module {

    @Op(name = "Mode", all = {"Progress", "Haste"})
    public String mode = "Progress";
    @OpChild(name = "Haste Level", min = 1, max = 5, parent = "Mode", dependency = "Haste")
    public int haste = 1;
    @OpChild(name = "Break Progress", max = 0.9f, inc = 0.01f, parent = "Mode", dependency = "Progress")
    public float progress = 0.65f;

    private boolean givenHaste;
    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() != EventPlayerPackets.Mode.PRE)
            return;
        switch (mode) {
            case "Progress":
                if (givenHaste && Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE))
                    Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
                if (Wrapper.INSTANCE.getIInteractionManager().getBlockBreakProgress() > progress)
                    Wrapper.INSTANCE.getIInteractionManager().setBlockBreakProgress(1);
                givenHaste = false;
                break;
            case "Haste":
                givenHaste = true;
                if (Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE) && Wrapper.INSTANCE.getLocalPlayer().getStatusEffect(StatusEffects.HASTE).getAmplifier() > haste - 1)
                    Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
                if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE)
                    Wrapper.INSTANCE.getLocalPlayer().addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 5200, haste - 1));
                break;
        }
        Wrapper.INSTANCE.getIInteractionManager().setBlockBreakingCooldown(0);
        this.setSuffix(mode);
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
        super.onDisable();
    }

}
