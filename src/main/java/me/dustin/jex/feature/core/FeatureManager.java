package me.dustin.jex.feature.core;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        Collections.sort(features, new Comparator<Feature>() {
            public int compare(Feature mod, Feature mod1) {
                int mod1s = 0;
                int mod2s = 0;
                String alph = "abcdefghijklmnopqrstuvwxyz";
                for (int i = 0; i < alph.length(); i++) {
                    if ((alph.charAt(i) + "").equalsIgnoreCase(mod.getName().substring(0, 1))) {
                        mod1s = i;
                    }
                    if ((alph.charAt(i) + "").equalsIgnoreCase(mod1.getName().substring(0, 1))) {
                        mod2s = i;
                    }
                }
                if (mod1s >= mod2s) {
                    return 1;
                }
                if (mod1s < mod2s) {
                    return -1;
                }
                return 1;
            }
        });
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }
}
