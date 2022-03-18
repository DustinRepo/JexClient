package me.dustin.jex.feature.extension;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import me.dustin.jex.feature.mod.core.Feature;

import java.util.ArrayList;

public abstract class FeatureExtension {

    private static ArrayList<FeatureExtension> featureExtensions = new ArrayList<>();
    private String name;
    private Class<? extends Feature> module;

    public FeatureExtension(String name, Class<? extends Feature> module) {
        this.name = name;
        this.module = module;
        featureExtensions.add(this);
    }

    public static FeatureExtension get(String name, Feature feature) {
        for (FeatureExtension featureExtension : featureExtensions) {
            if (featureExtension.module == feature.getClass() && featureExtension.name.equalsIgnoreCase(name)) {
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
        return name;
    }
}
