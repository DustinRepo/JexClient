package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Remove rain and snow")
public class NoWeather extends Feature {

    @EventListener(events = {EventRenderRain.class})
    public void run(EventRenderRain event) {
        event.cancel();
    }

}
