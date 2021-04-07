package me.dustin.jex.module.core;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public enum ModuleManager {
    INSTANCE;
    private ArrayList<Module> modules = new ArrayList<>();

    public void initializeModuleManager() {
        this.getModules().clear();

        Reflections reflections = new Reflections("me.dustin.jex.module", new org.reflections.scanners.Scanner[0]);

        Set<Class<? extends Module>> allClasses = reflections.getSubTypesOf(Module.class);
        allClasses.forEach(clazz -> {
            try {
                Module instance = clazz.newInstance();
                this.getModules().add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        Collections.sort(modules, new Comparator<Module>() {
            public int compare(Module mod, Module mod1) {
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

    public ArrayList<Module> getModules() {
        return modules;
    }
}
