package me.dustin.jex.feature.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JexPlugin {
    private final Class<?> mainClass;

    private Object mainClassInstance;
    private boolean isEnabled;

    private final PluginInfo info;
    public JexPlugin(Class<?> mainClass, PluginInfo pluginInfo) {
        this.mainClass = mainClass;
        this.info = pluginInfo;
    }
    public static void clientLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getMainClassInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(ClientLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getMainClassInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void fabricLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getMainClassInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(FabricLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getMainClassInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void featuresLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getMainClassInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(FeaturesLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getMainClassInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void commandsLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getMainClassInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(CommandsLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getMainClassInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void disable(JexPlugin jexPlugin) {
        Class<?> mainClass = jexPlugin.getMainClassInstance().getClass();
        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(DisablePlugin.class)) {
                try {
                    declaredMethod.invoke(jexPlugin.getMainClassInstance());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void enable(JexPlugin jexPlugin) {
        Class<?> mainClass = jexPlugin.getMainClassInstance().getClass();
        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(EnablePlugin.class)) {
                try {
                    declaredMethod.invoke(jexPlugin.getMainClassInstance());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Class<?> getMainClass() {
        return mainClass;
    }

    public PluginInfo getInfo() {
        return info;
    }

    public Object getMainClassInstance() {
        if (mainClassInstance == null) {
            try {
                mainClassInstance = getMainClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mainClassInstance;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface FeaturesLoad {
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandsLoad {
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ClientLoad {
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FabricLoad {
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DisablePlugin {
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EnablePlugin {
    }

    public static class PluginInfo {
        private final String name;
        private final String version;
        private final String description;
        private final String iconFile;
        private final String[] authors;
        private final boolean allowDisable;

        private final String mixinFile;

        public PluginInfo(String name, String version, String mixinFile, String description, String iconFile, String[] authors, boolean allowDisable) {
            this.name = name;
            this.version = version;
            this.mixinFile = mixinFile;
            this.description = description;
            this.iconFile = iconFile;
            this.authors = authors;
            this.allowDisable = allowDisable;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getDescription() {
            return description;
        }

        public String getIconFile() {
            return iconFile;
        }

        public String[] getAuthors() {
            return authors;
        }

        public boolean isAllowDisable() {
            return allowDisable;
        }

        public String getMixinFile() {
            return mixinFile;
        }
    }
}
