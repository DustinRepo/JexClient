package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventRenderTick;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

public class Timer extends Feature {

    @Op(name = "Speed", min = 0.1f, max = 5, inc = 0.1f)
    public float speed = 2;

    public Timer() {
        super(Category.WORLD, "Speed up or slow down the game");
    }

    @EventPointer
    private final EventListener<EventRenderTick> eventRenderTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer() == null && Wrapper.INSTANCE.getWorld() == null)
            this.setState(false);
        if (getState()) {
            event.timeScale = 1000 / (20.f * speed);
        } else {
            event.timeScale = 1000 / 20.f;
            EventManager.unregister(this);
        }
    });

    @Override
    public void onDisable() {
    }
}
