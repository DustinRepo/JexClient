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
import java.lang.Math;

public class AntiKnockback extends Feature {

    public final Property<Integer> pxProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentX")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> pyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentY")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> pzProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentZ")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> epxProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionX")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> epyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionY")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> epzProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionZ")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
   
    public AntiKnockback() {
        super(Category.COMBAT);
    }

    @EventPointer
    private final EventListener<EventExplosionVelocity> eventExplosionVelocityEventListener = new EventListener<>(event -> {
        float epx = epxProperty.value() / 100.0f;
        float epy = epyProperty.value() / 100.0f;
        float epz = epzProperty.value() / 100.0f;
        event.setMultX(epx);
        event.setMultY(epy);
        event.setMultZ(epz);

    });

    @EventPointer
    private final EventListener<EventPlayerVelocity> eventPlayerVelocityEventListener = new EventListener<>(event -> {
        float px = pxProperty.value() / 100.0f;
        float py = pyProperty.value() / 100.0f;
        float pz = pzProperty.value() / 100.0f;
        event.setVelocityX((int)(event.getVelocityX() * px));
        event.setVelocityY((int)(event.getVelocityY() * py));
        event.setVelocityZ((int)(event.getVelocityZ() * pz));
    });
}
