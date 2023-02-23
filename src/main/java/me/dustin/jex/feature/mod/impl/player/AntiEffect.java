package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.effect.StatusEffects;
import me.dustin.jex.feature.mod.core.Feature;

public class AntiEffect extends Feature {

    public final Property<Boolean> blindnessProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Blindness")
            .value(true)
            .build();
    public final Property<Boolean> nauseaProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Nausea")
            .value(true)
            .build();
    public final Property<Boolean> miningFatigueProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Mining Fatigue")
            .value(true)
            .build();
    public final Property<Boolean> weaknessProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Weakness")
            .value(true)
            .build();
    public final Property<Boolean> weaknessProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Slowness")
            .value(true)
            .build();
    public final Property<Boolean> hungerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hunger")
            .value(true)
            .build();
    public final Property<Boolean> poisonProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Poison")
            .value(true)
            .build();
    public final Property<Boolean> levitationProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Levitation")
            .value(true)
            .build();
    public final Property<Boolean> slowFallingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Slow Falling")
            .value(true)
            .build();
    public final Property<Boolean> darknessProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Darkness")
            .value(true)
            .build();

    public AntiEffect() {
        super(Category.PLAYER, "Remove certain negative effects from yourself.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (blindnessProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.BLINDNESS);
        if (nauseaProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.NAUSEA);
        if (miningFatigueProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.MINING_FATIGUE);
        if (wekanessProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.WEAKNESS);
        if (hungerProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.HUNGER);
        if (poisonProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.POISON);
        if (levitationProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.LEVITATION);
        if (slowFallingProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.SLOW_FALLING);
        if (darknessProperty.value())
            Wrapper.INSTANCE.getLocalPlayer().removeStatusEffect(StatusEffects.DARKNESS);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
