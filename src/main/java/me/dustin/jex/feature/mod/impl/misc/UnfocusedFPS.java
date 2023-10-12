package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventGetFramerateLimit;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;

public class UnfocusedFPS extends Feature {

    public final Property<Integer> unfocusedFPSProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Unfocused FPS")
            .value(15)
            .min(1)
            .max(25)
            .build();

    public UnfocusedFPS() {
        super(Category.MISC);
        setState(true);
    }

    @EventPointer
    private final EventListener<EventGetFramerateLimit> eventGetFramerateLimitEventListener = new EventListener<>(event -> {
        boolean focused = Wrapper.INSTANCE.getMinecraft().isWindowFocused();
        if (!focused) {
            event.setLimit(unfocusedFPSProperty.value());
        }
    });
}
