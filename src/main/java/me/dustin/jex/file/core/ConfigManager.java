package me.dustin.jex.file.core;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Set;

public enum ConfigManager {
    INSTANCE;

    private final ArrayList<ConfigFile> configFiles = new ArrayList<>();

    public void init() {
        Reflections reflections = new Reflections("me.dustin.jex.file.impl");
        Set<Class<? extends ConfigFile>> allClasses = reflections.getSubTypesOf(ConfigFile.class);
        allClasses.forEach(clazz -> {
            try {
                ConfigFile instance = clazz.newInstance();
                this.getConfigFiles().add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveAll() {
        getConfigFiles().forEach(ConfigFile::write);
    }

    public ArrayList<ConfigFile> getConfigFiles() {
        return configFiles;
    }

    public ConfigFile get(Class<? extends ConfigFile> clazz) {
        for (ConfigFile configFile : getConfigFiles()) {
            if (configFile.getClass() == clazz)
                return configFile;
        }
        return null;
    }
}
