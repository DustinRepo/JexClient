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
            .inc(10)
            .build();
    public final Property<Integer> percentyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentY")
            .value(0)
            .min(-100)
            .max(100)
            .inc(10)
            .build();
    public final Property<Integer> percentzProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("PercentZ")
            .value(0)
            .min(-100)
            .max(100)
            .inc(10)
            .build();

    public AntiKnockback() {
        super(Category.COMBAT, "Changes knockback from the player.");
    }

    @EventPointer
    private final EventListener<EventExplosionVelocity> eventExplosionVelocityEventListener = new EventListener<>(event -> {
        float percx = percentxProperty.value() / 100.0f;
        float percy = percentyProperty.value() / 100.0f;
        float percz = percentzProperty.value() / 100.0f;
        if (percentxProperty.value() == 0){
        if (percentyProperty.value() == 0){
        if (percentzProperty.value() == 0){
            event.cancel();
        }
        }
        }
        else {
            event.setMultX(percx);
            event.setMultY(percy);
            event.setMultZ(percz);
        }
    });

    @EventPointer
    private final EventListener<EventPlayerVelocity> eventPlayerVelocityEventListener = new EventListener<>(event -> {
        float percx = percentxProperty.value() / 100.0f;
        float percy = percentyProperty.value() / 100.0f;
        float percz = percentzProperty.value() / 100.0f;
        if (percentxProperty.value() == 0) {
        if (percentyProperty.value() == 0) {
        if (percentzProperty.value() == 0) {
            event.cancel();
         }
         }
         }
        else {
            event.setVelocityX((int)(event.getVelocityX() * percx));
            event.setVelocityY((int)(event.getVelocityY() * percy));
            event.setVelocityZ((int)(event.getVelocityZ() * percz));
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        float averageof = (percentxProperty.value() + percentyProperty.value() + percentzProperty.value()) / 3.0f;
        int intstring = Math.round(averageof);
        String stringint = Integer.toString(intstring);
        setSuffix(stringint); 
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
