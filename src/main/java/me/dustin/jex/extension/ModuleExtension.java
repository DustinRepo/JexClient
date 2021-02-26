package me.dustin.jex.extension;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import me.dustin.jex.module.core.Module;

import java.util.ArrayList;

public abstract class ModuleExtension {

    private static ArrayList<ModuleExtension> moduleExtensions = new ArrayList<>();
    private String name;
    private Class<? extends Module> module;

    public ModuleExtension(String name, Class<? extends Module> module) {
        this.name = name;
        this.module = module;
        moduleExtensions.add(this);
    }

    public static ModuleExtension get(String name, Module module) {
        for (ModuleExtension moduleExtension : moduleExtensions) {
            if (moduleExtension.module == module.getClass() && moduleExtension.name.equalsIgnoreCase(name)) {
                return moduleExtension;
            }
        }
        return null;
    }

    public abstract void pass(Event event);

    public void enable() {
    }

    public void disable() {
    }

    public Class<? extends Module> getModule() {
        return module;
    }

    public String getName() {
        return name;
    }
}
