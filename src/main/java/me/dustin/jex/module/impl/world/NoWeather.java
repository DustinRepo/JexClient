package me.dustin.jex.module.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "NoWeather", category = ModCategory.WORLD, description = "Remove rain and snow")
public class NoWeather extends Module {

    @EventListener(events = {EventRenderRain.class})
    public void run(EventRenderRain event) {
        event.cancel();
    }

}
