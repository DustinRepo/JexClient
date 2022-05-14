package me.dustin.jex.feature.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.JexClient;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public record JexPlugin(String name, Object instance, String description, String[] authors, boolean allowDisable) {
    private static final ArrayList<JexPlugin> plugins = new ArrayList<>();
    public static void add(String name, Object instance, String description, String[] authors, boolean allowDisable) {
        plugins.add(new JexPlugin(name, instance, description, authors, allowDisable));
    }
    public static void clientLoad() {
        plugins.forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.instance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(ClientLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.instance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void fabricLoad() {
        plugins.forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.instance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(FabricLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.instance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void featuresLoad() {
        plugins.forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.instance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(FeaturesLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.instance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void commandsLoad() {
        plugins.forEach(jexPlugin -> {
            Class<?> mainClass = jexPlugin.instance().getClass();
            for (Method declaredMethod : mainClass.getDeclaredMethods()) {
                if (declaredMethod.isAnnotationPresent(CommandsLoad.class)) {
                    try {
                        declaredMethod.invoke(jexPlugin.instance());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void disable(JexPlugin jexPlugin) {
        Class<?> mainClass = jexPlugin.instance().getClass();
        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(DisablePlugin.class)) {
                try {
                    declaredMethod.invoke(jexPlugin.instance());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void enable(JexPlugin jexPlugin) {
        Class<?> mainClass = jexPlugin.instance().getClass();
        for (Method declaredMethod : mainClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(EnablePlugin.class)) {
                try {
                    declaredMethod.invoke(jexPlugin.instance());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void loadPlugins() {
        File pluginsFolder = new File(ModFileHelper.INSTANCE.getJexDirectory(), "plugins");
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
            return;
        }
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("JexPlugin.json");
            if (inputStream != null) {
                JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(read(inputStream), JsonObject.class);
                loadFromJson(jsonObject, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (File file : pluginsFolder.listFiles()) {
            try {
                if (file.getName().endsWith(".jar")) {
                    URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
                    JarFile jarFile = new JarFile(file);
                    final Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        final JarEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().equalsIgnoreCase("JexPlugin.json")) {
                            InputStream inputStream = jarFile.getInputStream(entry);

                            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(read(inputStream), JsonObject.class);
                            loadFromJson(jsonObject, urlClassLoader);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //just to make sure there's not some weird order-dependant loads
        Collections.shuffle(plugins);
    }

    private static String read(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try {
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            for (String line; (line = reader.readLine()) != null; ) {
                sb.append(line).append("\n");
            }
        } catch (NullPointerException e) {}
        return sb.toString();
    }

    private static void loadFromJson(JsonObject jsonObject, URLClassLoader urlClassLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String name = jsonObject.get("name").getAsString();
        String mainClass = jsonObject.get("mainClass").getAsString();
        String description = jsonObject.get("description").getAsString();
        ArrayList<String> list = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("authors");
        jsonArray.forEach(jsonElement -> list.add(jsonElement.getAsString()));
        boolean allowDisable = jsonObject.get("allowDisable").getAsBoolean();
        Class<?> mainClazz = urlClassLoader != null ? urlClassLoader.loadClass(mainClass) : Class.forName(mainClass);

        add(name, mainClazz.newInstance(), description, toArray(list), allowDisable);
        JexClient.INSTANCE.getLogger().info("Found Plugin: %s".formatted(name));
    }

    private static String[] toArray(ArrayList<String> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static JexPlugin get(String name) {
        for (JexPlugin plugin : plugins) {
            if (plugin.name().equals(name))
                return plugin;
        }
        return null;
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
