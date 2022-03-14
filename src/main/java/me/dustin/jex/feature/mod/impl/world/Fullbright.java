package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Goodbye, darkness. You were never my friend.")
public class Fullbright extends Feature {

    @Op(name = "Brightness", max = 50, min = 10)
    public int brightness = 50;
    @Op(name = "Reset Gamma", max = 1, inc = 0.05f)
    public float resetGamma = 1;

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getOptions() == null)
            return;
        double gamma = Wrapper.INSTANCE.getOptions().gamma;
        if (!getState()) {
            if (gamma > resetGamma)
                Wrapper.INSTANCE.getOptions().gamma -= 0.5f;
            else
                super.onDisable();
        } else {
            if (gamma < brightness)
                Wrapper.INSTANCE.getOptions().gamma += 0.5f;
                else  if (gamma > brightness)
                Wrapper.INSTANCE.getOptions().gamma = brightness;
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getOptions().gamma > 20)
            Wrapper.INSTANCE.getOptions().gamma = 20;
    }
}
