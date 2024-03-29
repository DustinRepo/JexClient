package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.impl.movement.fly.Fly;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.Freecam;

public class FallSpeed extends Feature {

    public final Property<Float> fallDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Fall Distance")
            .description("The distance before the speed option kicks in")
            .value(3f)
            .min(0)
            .max(10)
            .inc(0.5f)
            .build();
    public final Property<Float> speedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Speed")
            .value(0.5f)
            .min(0.1f)
            .max(15)
            .inc(0.1f)
            .build();

    public FallSpeed() {
        super(Category.MOVEMENT, "Change your fall speed");
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        if (Feature.getState(Fly.class) || Feature.getState(Freecam.class))
            return;
        if (Wrapper.INSTANCE.getLocalPlayer().fallDistance > fallDistanceProperty.value() && !Wrapper.INSTANCE.getLocalPlayer().isOnGround()) {
            event.setY(-speedProperty.value());
        }
    });
}
