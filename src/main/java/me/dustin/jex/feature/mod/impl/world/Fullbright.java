package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetSimpleOption;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.option.SimpleOption;

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
        SimpleOption<Double> gammaOption = Wrapper.INSTANCE.getOptions().getGamma();
        if (Wrapper.INSTANCE.getLocalPlayer() == null && gammaOption.getValue() > 1) {
            gammaOption.setValue(1.0);
            return;
        }

            double gamma = gammaOption.getValue();
        if (!getState()) {
            if (gamma > resetGamma)
                gammaOption.setValue(gamma - 0.5);
            else
                super.onDisable();
        } else {
            if (gamma < brightness)
                    gammaOption.setValue(gamma + 0.5);
                else  if (gamma > brightness)
                    gammaOption.setValue((double) brightness);
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetSimpleOption> eventSetSimpleOptionEventListener = new EventListener<>(event -> {
        SimpleOption<Double> gammaOption = Wrapper.INSTANCE.getOptions().getGamma();
        if (event.getSimpleOption() == gammaOption)
            event.setShouldIgnoreCheck(true);
    });

    @Override
    public void onDisable() {
        SimpleOption<Double> gammaOption = Wrapper.INSTANCE.getOptions().getGamma();
        if (gammaOption.getValue() > 20)
            gammaOption.setValue(20.0);
    }
}
