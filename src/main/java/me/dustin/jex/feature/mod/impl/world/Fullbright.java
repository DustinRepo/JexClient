package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetOptionInstance;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.OptionInstance;
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
        OptionInstance<Double> gammaOption = Wrapper.INSTANCE.getOptions().gamma();
        if (Wrapper.INSTANCE.getLocalPlayer() == null && gammaOption.get() > 1) {
            gammaOption.set(1.0);
            return;
        }

            double gamma = gammaOption.get();
        if (!getState()) {
            if (gamma > resetGamma)
                gammaOption.set(gamma - 0.5);
            else
                super.onDisable();
        } else {
            if (gamma < brightness)
                    gammaOption.set(gamma + 0.5);
                else  if (gamma > brightness)
                    gammaOption.set((double) brightness);
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetOptionInstance> eventSetSimpleOptionEventListener = new EventListener<>(event -> {
        OptionInstance<Double> gammaOption = Wrapper.INSTANCE.getOptions().gamma();
        if (event.getOptionInstance() == gammaOption)
            event.setShouldIgnoreCheck(true);
    });

    @Override
    public void onDisable() {
        OptionInstance<Double> gammaOption = Wrapper.INSTANCE.getOptions().gamma();
        if (gammaOption.get() > 20)
            gammaOption.set(20.0);
    }
}
