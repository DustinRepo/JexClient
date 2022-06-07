package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventPlayerVelocity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;

public class AntiKnockback extends Feature {

    public final Property<Integer> percentProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Percent")
            .value(0)
            .min(-300)
            .max(300)
            .inc(10)
            .build();

    public AntiKnockback() {
        super(Category.COMBAT, "Remove all knockback from the player.");
    }

    @EventPointer
    private final EventListener<EventExplosionVelocity> eventExplosionVelocityEventListener = new EventListener<>(event -> {
        float perc = percentProperty.value() / 100.0f;
        if (percentProperty.value() == 0)
            event.cancel();
        else {
            event.setMultX(perc);
            event.setMultY(perc);
            event.setMultZ(perc);
        }
    });

    @EventPointer
    private final EventListener<EventPlayerVelocity> eventPlayerVelocityEventListener = new EventListener<>(event -> {
        float perc = percentProperty.value() / 100.0f;
        if (percentProperty.value() == 0)
            event.cancel();
        else {
            event.setVelocityX((int)(event.getVelocityX() * perc));
            event.setVelocityY((int)(event.getVelocityY() * perc));
            event.setVelocityZ((int)(event.getVelocityZ() * perc));
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(percentProperty.value() + "%");
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
