package me.dustin.jex.module.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@ModClass(name = "Speedmine", category = ModCategory.PLAYER, description = "Break blocks faster")
public class SpeedMine extends Module {

    @Op(name = "Haste", min = 1, max = 5)
    public int haste = 1;

    @EventListener(events = {EventPlayerPackets.class})
    public void run(Event event) {
        if (Wrapper.INSTANCE.getLocalPlayer().hasStatusEffect(StatusEffects.HASTE) && Wrapper.INSTANCE.getLocalPlayer().getStatusEffect(StatusEffects.HASTE).getAmplifier() > haste - 1)
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
        if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE)
            Wrapper.INSTANCE.getLocalPlayer().addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 5200, haste - 1));
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null)
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HASTE);
        super.onDisable();
    }
}
