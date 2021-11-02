package me.dustin.jex.feature.mod.core;

import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.jex.helper.file.ClassHelper;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Set;

public enum FeatureManager {
    INSTANCE;
    private final ArrayList<Feature> features = new ArrayList<>();

    public void initializeFeatureManager() {
        this.getFeatures().forEach(feature -> feature.setState(false));
        this.getFeatures().clear();

        //TODO: better method of doing this without a library
        Reflections reflections = new Reflections("me.dustin.jex.feature.mod.impl");
        Set<Class<? extends Feature>> allClasses = reflections.getSubTypesOf(Feature.class);
        allClasses.forEach(clazz -> {
            try {
                Feature instance = clazz.newInstance();
                this.getFeatures().add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        features.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }
}
