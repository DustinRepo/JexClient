package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventRenderTick;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

public class Timer extends Feature {

    public final Property<Float> speedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Speed")
            .value(2f)
            .min(0.1f)
            .max(25f)
            .inc(0.1f)
            .build();

    public Timer() {
        super(Category.WORLD, "Speed up or slow down the game");
    }

    @EventPointer
    private final EventListener<EventRenderTick> eventRenderTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null && Wrapper.INSTANCE.getWorld() == null)
            this.setState(false);
        if (getState()) {
            event.timeScale = 1000 / (20.f * speedProperty.value());
        } else {
            event.timeScale = 1000 / 20.f;
            EventManager.unregister(this);
        }
    });

    @Override
    public void onDisable() {
    }
}
