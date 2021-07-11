package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventRenderTick;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(name = "Timer", category = Feature.Category.WORLD, description = "Speed up or slow down the game")
public class Timer extends Feature {

    @Op(name = "Speed", min = 0.1f, max = 5, inc = 0.1f)
    public float speed = 2;

    @EventListener(events = {EventRenderTick.class})
    private void runMethod(EventRenderTick eventRenderTick) {
        if (Wrapper.INSTANCE.getLocalPlayer() == null && Wrapper.INSTANCE.getWorld() == null)
            this.setState(false);
        if (getState()) {
            eventRenderTick.timeScale = 1000 / (20.f * speed);
        } else {
            eventRenderTick.timeScale = 1000 / 20.f;
            while (EventAPI.getInstance().alreadyRegistered(this))
                EventAPI.getInstance().unregister(this);
        }
    }

    @Override
    public void onDisable() {
    }
}
