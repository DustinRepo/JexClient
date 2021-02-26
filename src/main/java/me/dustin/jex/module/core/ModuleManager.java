package me.dustin.jex.module.core;

import com.google.common.collect.Maps;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Set;

public enum ModuleManager {
    INSTANCE;
    private HashMap<Class<? extends Module>, Module> modules = Maps.newHashMap();

    public void initializeModuleManager() {
        this.getModules().clear();

        Reflections reflections = new Reflections("me.dustin.jex", new org.reflections.scanners.Scanner[0]);
        //Reflections reflections = new Reflections("me.dustin.jex.h.b", new org.reflections.scanners.Scanner[0]);

        Set<Class<? extends Module>> allClasses = reflections.getSubTypesOf(Module.class);
        allClasses.forEach(clazz -> {
            try {
                Module instance = clazz.newInstance();
                this.getModules().put(clazz, instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public HashMap<Class<? extends Module>, Module> getModules() {
        return modules;
    }
}
