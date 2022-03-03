package me.dustin.jex.file.core;

import me.dustin.jex.file.impl.AltFile;
import me.dustin.jex.helper.misc.ClassHelper;

import java.util.ArrayList;
import java.util.List;

public enum ConfigManager {
    INSTANCE;

    private final ArrayList<ConfigFile> configFiles = new ArrayList<>();

    public void init() {
        List<Class<?>> classList = ClassHelper.INSTANCE.getClasses("me.dustin.jex.file.impl", ConfigFile.class);
        classList.forEach(clazz -> {
            try {
                ConfigFile instance = (ConfigFile) clazz.newInstance();
                this.getConfigFiles().add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveAll() {
        getConfigFiles().forEach(configFile -> {
            if (!(configFile instanceof AltFile))
                configFile.write();
        });
    }

    public ArrayList<ConfigFile> getConfigFiles() {
        return configFiles;
    }

    public <T extends ConfigFile> T get(Class<T> clazz) {
        for (ConfigFile configFile : getConfigFiles()) {
            if (configFile.getClass() == clazz)
                return clazz.cast(configFile);
        }
        return null;
    }
}
