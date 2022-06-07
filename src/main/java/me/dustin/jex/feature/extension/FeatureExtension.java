package me.dustin.jex.feature.extension;

import me.dustin.events.core.Event;
import me.dustin.jex.feature.mod.core.Feature;

import java.util.ArrayList;
import java.util.Locale;

public abstract class FeatureExtension {

    private static final ArrayList<FeatureExtension> featureExtensions = new ArrayList<>();
    private final Enum<?> value;
    private final Class<? extends Feature> module;

    public FeatureExtension(Enum<?> value, Class<? extends Feature> module) {
        this.module = module;
        this.value = value;
        featureExtensions.add(this);
    }

    public static FeatureExtension get(Enum<?> value, Feature feature) {
        for (FeatureExtension featureExtension : featureExtensions) {
            if (featureExtension.module == feature.getClass() && featureExtension.value == value) {
                return featureExtension;
            }
        }
        return null;
    }

    public abstract void pass(Event event);

    public void enable() {
    }

    public void disable() {
    }

    public Class<? extends Feature> getModule() {
        return module;
    }

    public String getName() {
        return value.name().substring(0, 1).toUpperCase() + value.name().substring(1).toLowerCase();
    }

    public Enum<?> getValue() {
        return value;
    }
}
