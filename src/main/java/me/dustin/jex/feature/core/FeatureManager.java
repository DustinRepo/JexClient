package me.dustin.jex.feature.core;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Set;

public enum FeatureManager {
    INSTANCE;
    private ArrayList<Feature> features = new ArrayList<>();

    public void initializeFeatureManager() {
        this.getFeatures().clear();

        Reflections reflections = new Reflections("me.dustin.jex.feature", new org.reflections.scanners.Scanner[0]);

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
