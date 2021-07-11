package me.dustin.jex.feature.mod.core;

import com.google.common.collect.Maps;
import me.dustin.events.api.EventAPI;
import me.dustin.jex.gui.click.window.listener.ButtonListener;
import me.dustin.jex.feature.option.Option;
import me.dustin.jex.feature.option.OptionManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Map;

public class Feature {

    private String name;
    private String displayName;
    private String description;
    private boolean state;
    private boolean visible;
    private int key;
    private Feature.Category featureCategory;

    public Feature() {
        this.name = this.getClass().getAnnotation(Feature.Manifest.class).name();
        this.displayName = this.getClass().getAnnotation(Feature.Manifest.class).name();
        this.description = this.getClass().getAnnotation(Feature.Manifest.class).description();
        this.featureCategory = this.getClass().getAnnotation(Feature.Manifest.class).category();
        this.key = this.getClass().getAnnotation(Feature.Manifest.class).key();
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

    public static ArrayList<Feature> getModules(Feature.Category category) {
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

    public Feature.Category getFeatureCategory() {
        return featureCategory;
    }

    public void setFeatureCategory(Feature.Category featureCategory) {
        this.featureCategory = featureCategory;
    }

    public Map<String, ButtonListener> addButtons() {return Maps.newHashMap();}

    public enum Category {
        COMBAT, PLAYER, MOVEMENT, WORLD, VISUAL, MISC
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Manifest {
        String name();
        Feature.Category category();
        String description();
        int key() default 0;
    }

}
