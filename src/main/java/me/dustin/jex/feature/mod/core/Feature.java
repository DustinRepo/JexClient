package me.dustin.jex.feature.mod.core;

import me.dustin.jex.feature.keybind.Keybind;
import me.dustin.jex.feature.mod.impl.render.hud.Hud;
import me.dustin.events.EventManager;
import me.dustin.jex.feature.property.Property;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

public class Feature {
    private static final Feature dummyFeature = new Feature(Category.COMBAT, "Error");
    private String name;
    private String displayName;
    private String description;
    private boolean state;
    private final Category category;
    private final boolean defaultState;

    private final Property<Boolean> visibleProperty;
    
    public Feature(Category category) {
        this("", category, "", false, true, 0);
    }
    public Feature(Category category, String description) {
        this("", category, description, false, true, 0);
    }

    public Feature(Category category, String description, int key) {
        this("", category, description, false, true, key);
    }

    public Feature(String name, Category category, String description) {
        this(name, category, description, false, true, 0);
    }

    public Feature(String name, Category category, String description, boolean state, boolean visible, int key) {
        this.name = name;
        if (this.name.isEmpty())
            this.name = this.getClass().getSimpleName();
        this.displayName = this.name;
        this.description = description;
        this.category = category;
        if (key != 0)
            Keybind.add(key, "t " + this.getName(), true);
        this.visibleProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
                .name("Is Visible")
                .description("Whether or not this mod is visible on the HUD.")
                .value(visible)
                .build();
        this.defaultState = state;
    }

    public static <T extends Feature> T get(Class<T> clazz) {
        return clazz.cast(Feature.getFeature(clazz));
    }

    private static Feature getFeature(Class<? extends Feature> clazz) {
        for (Feature feature : FeatureManager.INSTANCE.getFeatures()) {
            if (feature.getClass() == clazz)
                return feature;
        }
        return dummyFeature;
    }

    public static Feature get(String name) {
        return FeatureManager.INSTANCE.getFeatures().stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static boolean getState(Class<? extends Feature> clazz) {
        return get(clazz).getState();
    }

    public static ArrayList<Feature> getModules(Category category) {
        ArrayList<Feature> features = new ArrayList<>();
        FeatureManager.INSTANCE.getFeatures().forEach(module -> {
            if (module.getCategory() == category)
                features.add(module);
        });
        return features;
    }

    public void toggleState() {
        this.setState(!this.getState());
    }

    public void onEnable() {
        EventManager.register(this);
    }

    public void onDisable() {
        EventManager.unregister(this);
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
        if (!Hud.INSTANCE.suffixesProperty.value()) {
            this.setDisplayName(this.getName());
            return;
        }
        if (suffix.isEmpty()) {
            this.setDisplayName(this.getName());
            return;
        }
        this.setDisplayName(this.getName() + "\2477: \2478" + suffix);
    }

    public void setSuffix(Enum<?> suffix) {
        setSuffix(WordUtils.capitalize(suffix.name().toLowerCase().replace("_", " ")));
    }

    public void setSuffix(Enum<?> one, Enum<?> two) {
        setSuffix(WordUtils.capitalize(one.name().toLowerCase().replace("_", " ")) + " : " + WordUtils.capitalize(two.name().toLowerCase().replace("_", " ")));
    }

    public boolean isVisible() {
        return visibleProperty.value();
    }

    public void setVisible(boolean visible) {
        this.visibleProperty.setValue(visible);
    }

    public Category getCategory() {
        return category;
    }

    public void loadFeature() {
        //fuck-ass workaround for having mods enabled by default in the code messing with the event manager
        if (defaultState)
            setState(true);
    }

    public void setKey(int key) {
        Keybind keybind = Keybind.get("t " + getName());
        if (keybind != null)
            Keybind.remove(keybind);
        Keybind.add(key, "t " + getName(), true);
    }


    public int getKey() {
        Keybind keybind = Keybind.get("t " + getName());
        if (keybind != null)
            return keybind.key();
        else return 0;
    }
}
