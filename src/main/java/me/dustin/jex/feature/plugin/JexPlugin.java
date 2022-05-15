package me.dustin.jex.feature.plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JexPlugin {
    private final String name;
    private final Class<?> clazz;
    private final String description;
    private final String[] authors;
    private final boolean allowDisable;
    private Object instance;
    private String mixinFile = "";
    public JexPlugin(String name, Class<?> clazz, String description, String[] authors, boolean allowDisable) {
        this.name = name;
        this.clazz = clazz;
        this.description = description;
        this.authors = authors;
        this.allowDisable = allowDisable;
    }
    public static void clientLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(ClientLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void fabricLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(FabricLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void featuresLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(FeaturesLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void commandsLoad() {
        JexPluginManager.INSTANCE.getPlugins().forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.getInstance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(CommandsLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.getInstance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void disable(JexPlugin jexPlugin) {
        Class<?> mainClass = jexPlugin.getInstance().getClass();
        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(DisablePlugin.class)) {
                try {
                    declaredMethod.invoke(jexPlugin.getInstance());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void enable(JexPlugin jexPlugin) {
        Class<?> mainClass = jexPlugin.getInstance().getClass();
        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(EnablePlugin.class)) {
                try {
                    declaredMethod.invoke(jexPlugin.getInstance());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAuthors() {
        return authors;
    }

    public boolean isAllowDisable() {
        return allowDisable;
    }

    public String getMixins() {
        return mixinFile;
    }

    public void setMixins(String mixinFile) {
        this.mixinFile = mixinFile;
    }

    public Object getInstance() {
        if (instance == null) {
            try {
                instance = getClazz().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return instance;
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
}
