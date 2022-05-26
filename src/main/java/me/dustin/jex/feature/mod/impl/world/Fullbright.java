package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventSetOptionInstance;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.option.SimpleOption;
import me.dustin.jex.feature.mod.core.Feature;

public class Fullbright extends Feature {

    public final Property<Double> brightnessProperty = new Property.PropertyBuilder<Double>(this.getClass())
            .name("Brightness")
            .value(50D)
            .min(10)
            .max(50)
            .inc(1)
            .build();
    public final Property<Double> resetGammaProperty = new Property.PropertyBuilder<Double>(this.getClass())
            .name("Reset Gamma")
            .description("The gamma to set the value to on disable.")
            .value(1D)
            .max(1)
            .inc(0.05f)
            .build();

    public Fullbright() {
        super(Category.WORLD, "Goodbye, darkness. You were never my friend.");
    }

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
            if (gamma > resetGammaProperty.value())
                gammaOption.setValue(gamma - 0.5);
            else
                super.onDisable();
        } else {
            if (gamma < brightnessProperty.value())
                    gammaOption.setValue(gamma + 0.5);
                else  if (gamma > brightnessProperty.value())
                    gammaOption.setValue(brightnessProperty.value());
        }
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetOptionInstance> eventSetSimpleOptionEventListener = new EventListener<>(event -> {
        SimpleOption<Double> gammaOption = Wrapper.INSTANCE.getOptions().getGamma();
        if (event.getOptionInstance() == gammaOption)
            event.setShouldIgnoreCheck(true);
    });

    @Override
    public void onDisable() {
        SimpleOption<Double> gammaOption = Wrapper.INSTANCE.getOptions().getGamma();
        if (gammaOption.getValue() > 20)
            gammaOption.setValue(20.0);
    }
}
