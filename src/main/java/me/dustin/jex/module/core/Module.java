package me.dustin.jex.module.core;

import me.dustin.events.api.EventAPI;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.Option;
import me.dustin.jex.option.OptionManager;

import java.util.ArrayList;

public class Module {

    private String name;
    private String displayName;
    private String description;
    private boolean state;
    private boolean visible;
    private int key;
    private ModCategory modCategory;

    public Module() {
        this.name = this.getClass().getAnnotation(ModClass.class).name();
        this.displayName = this.getClass().getAnnotation(ModClass.class).name();
        this.description = this.getClass().getAnnotation(ModClass.class).description();
        this.modCategory = this.getClass().getAnnotation(ModClass.class).category();
        this.visible = true;
    }

    public static Module get(Class<? extends Module> clazz) {
        return ModuleManager.INSTANCE.getModules().get(clazz);
    }

    public static Module get(String name) {
        return ModuleManager.INSTANCE.getModules().values().stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static ArrayList<Module> getModules(ModCategory category) {
        ArrayList<Module> modules = new ArrayList<>();
        ModuleManager.INSTANCE.getModules().values().forEach(module ->
        {
            if (module.getModCategory() == category)
                modules.add(module);
        });
        return modules;
    }

    public ArrayList<Option> getOptions() {
        ArrayList<Option> options = new ArrayList<Option>();
        OptionManager.get().getOptions().forEach(option ->
        {
            if (option.getModule() == this)
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

    public ModCategory getModCategory() {
        return modCategory;
    }

    public void setModCategory(ModCategory modCategory) {
        this.modCategory = modCategory;
    }


}
