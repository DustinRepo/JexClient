package me.dustin.jex.feature.core;

import me.dustin.events.api.EventAPI;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.Option;
import me.dustin.jex.option.OptionManager;

import java.util.ArrayList;

public class Feature {

    private String name;
    private String displayName;
    private String description;
    private boolean state;
    private boolean visible;
    private int key;
    private FeatureCategory featureCategory;

    public Feature() {
        this.name = this.getClass().getAnnotation(Feat.class).name();
        this.displayName = this.getClass().getAnnotation(Feat.class).name();
        this.description = this.getClass().getAnnotation(Feat.class).description();
        this.featureCategory = this.getClass().getAnnotation(Feat.class).category();
        this.visible = true;
    }

    public static Feature get(Class<? extends Feature> clazz) {
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (feature.getClass() == clazz)
                return feature;
        }
        return null;
    }

    public static Feature get(String name) {
        return FeatureManager.INSTANCE.getFeatures().stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static ArrayList<Feature> getModules(FeatureCategory category) {
        ArrayList<Feature> features = new ArrayList<>();
        FeatureManager.INSTANCE.getFeatures().forEach(module ->
        {
            if (module.getFeatureCategory() == category)
                features.add(module);
        });
        return features;
    }

    public ArrayList<Option> getOptions() {
        ArrayList<Option> options = new ArrayList<Option>();
        OptionManager.get().getOptions().forEach(option ->
        {
            if (option.getFeature() == this)
                options.add(option);
        });
        return options;
    }
    public void toggleState() {
        this.setState(!this.getState());
    }

    public void onEnable() {
        if (EventAPI.getInstance().alreadyRegistered(this))
            EventAPI.getInstance().unregister(this);
        EventAPI.getInstance().register(this);
    }

    public void onDisable() {
        while (EventAPI.getInstance().alreadyRegistered(this))
            EventAPI.getInstance().unregister(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
        if (this.getState()) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void setSuffix(String suffix) {
        if (suffix.isEmpty()) {
            this.setDisplayName(this.getName());
            return;
        }
        this.setDisplayName(this.getName() + "\2477: \2478" + suffix);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public FeatureCategory getFeatureCategory() {
        return featureCategory;
    }

    public void setFeatureCategory(FeatureCategory featureCategory) {
        this.featureCategory = featureCategory;
    }


}
