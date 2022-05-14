package me.dustin.jex.feature.mod.core;

import me.dustin.jex.feature.plugin.JexPlugin;
import me.dustin.jex.helper.misc.ClassHelper;

import java.util.ArrayList;
import java.util.List;

public enum FeatureManager {
    INSTANCE;
    private final ArrayList<Feature> features = new ArrayList<>();

    public void initializeFeatureManager() {
        this.getFeatures().forEach(feature -> feature.setState(false));
        this.getFeatures().clear();

        List<Class<?>> classList = ClassHelper.INSTANCE.getClasses("me.dustin.jex.feature.mod.impl", Feature.class);
        classList.forEach(clazz -> {
            try {
                Feature instance = (Feature) clazz.newInstance();
                instance.loadFeature();
                this.getFeatures().add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        JexPlugin.featuresLoad();
        features.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }
}
