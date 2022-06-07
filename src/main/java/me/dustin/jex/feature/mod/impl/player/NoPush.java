package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPushAwayFromEntity;
import me.dustin.jex.event.world.EventWaterVelocity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;

public class NoPush extends Feature {

    public final Property<Boolean> mobsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Mobs")
            .value(true)
            .build();
    public final Property<Boolean> waterProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Water")
            .value(true)
            .build();

    public NoPush() {
        super(Category.PLAYER, "Don't let others push you around.");
    }

    @EventPointer
    private final EventListener<EventWaterVelocity> eventWaterVelocityEventListener = new EventListener<>(event -> {
       if (waterProperty.value()) event.cancel();
    });

    @EventPointer
    private final EventListener<EventPushAwayFromEntity> eventPushAwayFromEntityEventListener = new EventListener<>(event -> {
       if (mobsProperty.value()) event.cancel();
    });
}
