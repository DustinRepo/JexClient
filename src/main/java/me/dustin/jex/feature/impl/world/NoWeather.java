package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "NoWeather", category = FeatureCategory.WORLD, description = "Remove rain and snow")
public class NoWeather extends Feature {

    @EventListener(events = {EventRenderRain.class})
    public void run(EventRenderRain event) {
        event.cancel();
    }

}
