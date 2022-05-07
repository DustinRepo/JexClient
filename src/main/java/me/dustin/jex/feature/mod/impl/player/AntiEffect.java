package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.world.effect.MobEffects;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Remove certain negative effects from yourself.")
public class AntiEffect extends Feature {

    @Op(name = "Blindness")
    public boolean blindness = true;
    @Op(name = "Nausea")
    public boolean nausea = true;
    @Op(name = "Mining Fatigue")
    public boolean miningFatigue = false;
    @Op(name = "Levitation")
    public boolean levitation = false;
    @Op(name = "Slow Falling")
    public boolean slowFalling = true;
    @Op(name = "Darkness")
    public boolean darkness = true;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (blindness)
            Wrapper.INSTANCE.getLocalPlayer().removeEffect(MobEffects.BLINDNESS);
        if (nausea)
            Wrapper.INSTANCE.getLocalPlayer().removeEffect(MobEffects.CONFUSION);
        if (miningFatigue)
            Wrapper.INSTANCE.getLocalPlayer().removeEffect(MobEffects.DIG_SLOWDOWN);
        if (levitation)
            Wrapper.INSTANCE.getLocalPlayer().removeEffect(MobEffects.LEVITATION);
        if (slowFalling)
            Wrapper.INSTANCE.getLocalPlayer().removeEffect(MobEffects.SLOW_FALLING);
        if (darkness)
            Wrapper.INSTANCE.getLocalPlayer().removeEffect(MobEffects.DARKNESS);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
