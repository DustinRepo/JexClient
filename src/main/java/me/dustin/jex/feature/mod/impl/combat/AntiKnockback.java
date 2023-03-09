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

    public final Property<Integer> percentxProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentX")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> percentyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentY")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> percentzProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentZ")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> epercentxProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionX")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> epercentyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("ExplosionY")
            .value(0)
            .min(-100)
            .max(100)
            .inc(2)
            .build();
    public final Property<Integer> epercentzProperty = new Property.PropertyBuilder<Integer>(this.getClass())
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
        float epx = epercentxProperty.value() / 100.0f;
        float epy = epercentyProperty.value() / 100.0f;
        float epz = epercentzProperty.value() / 100.0f;
        if (epercentxProperty.value() == 0){
        if (epercentyProperty.value() == 0){
        if (epercentzProperty.value() == 0){
            event.cancel();
        }
        }
        }
        else {
            event.setMultX(epx);
            event.setMultY(epy);
            event.setMultZ(epz);
        }
    });

    @EventPointer
    private final EventListener<EventPlayerVelocity> eventPlayerVelocityEventListener = new EventListener<>(event -> {
        float px = percentxProperty.value() / 100.0f;
        float py = percentyProperty.value() / 100.0f;
        float pz = percentzProperty.value() / 100.0f;
        if (percentxProperty.value() == 0) {
        if (percentyProperty.value() == 0) {
        if (percentzProperty.value() == 0) {
            event.cancel();
         }
         }
         }
        else {
            event.setVelocityX((int)(event.getVelocityX() * px));
            event.setVelocityY((int)(event.getVelocityY() * py));
            event.setVelocityZ((int)(event.getVelocityZ() * pz));
        }
    });
}
