package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

public class Hitboxes extends Feature {

    public final Property<Float> expansionProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Expansion")
            .value(0.1f)
            .min(0.1f)
            .max(1f)
            .inc(0.1f)
            .build();

    public Hitboxes() {
        super(Category.COMBAT, "Resize entity hitboxes to make them easier to hit");
    }

    @EventPointer
    private final EventListener<EventEntityHitbox> eventEntityHitboxEventListener = new EventListener<>(event -> {
        if (event.getEntity() == null || Wrapper.INSTANCE.getLocalPlayer() == null || event.getEntity().getId() == Wrapper.INSTANCE.getLocalPlayer().getId())
            return;
        event.setBox(event.getBox().expand(expansionProperty.value(), 0, expansionProperty.value()));
    });
}
