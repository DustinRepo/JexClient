package me.dustin.jex.feature.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.JsonHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public enum JexPluginManager {
    INSTANCE;
    private final Logger LOGGER = LogManager.getFormatterLogger("JexPlugins");
    private final ArrayList<JexPlugin> plugins = new ArrayList<>();

    public JexPlugin add(String name, String version, Class<?> clazz, String description, String[] authors, boolean allowDisable) {
        JexPlugin jexPlugin = new JexPlugin(name, version, clazz, description, authors, allowDisable);
        JexPluginManager.INSTANCE.getPlugins().add(jexPlugin);
        return jexPlugin;
    }
    public void loadPlugins() {
        File pluginsFolder = new File(new File(FabricLoader.getInstance().getGameDir().toString(), "JexClient"), "plugins");
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
            return;
        }
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("JexPlugin.json");
            if (inputStream != null) {
                JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(read(inputStream), JsonObject.class);
                loadFromJson(jsonObject, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (File file : pluginsFolder.listFiles()) {
            try {
                if (file.getName().endsWith(".jar")) {
                    JarFile jarFile = new JarFile(file);
                    final Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        final JarEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().equalsIgnoreCase("JexPlugin.json")) {
                            InputStream inputStream = jarFile.getInputStream(entry);

                            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(read(inputStream), JsonObject.class);
                            loadFromJson(jsonObject, file, jarFile);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //just to make sure there's not some weird order-dependant loads
        Collections.shuffle(getPlugins());
        LOGGER.info("Loaded Plugins: %d".formatted(getPlugins().size()));
        getPlugins().forEach(jexPlugin -> LOGGER.info("%s v%s".formatted(jexPlugin.getName(), jexPlugin.getVersion())));
    }

    private String read(InputStream inputStream) throws IOException {
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

    private void loadFromJson(JsonObject jsonObject, File fileOfJar, JarFile jarFile) throws ClassNotFoundException {
        String name = jsonObject.get("name").getAsString();
        String version = jsonObject.get("version").getAsString();
        String mainClass = jsonObject.get("mainClass").getAsString();
        String description = jsonObject.get("description").getAsString();
        ArrayList<String> list = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("authors");
        jsonArray.forEach(jsonElement -> list.add(jsonElement.getAsString()));
        boolean allowDisable = jsonObject.get("allowDisable").getAsBoolean();
        if (jsonObject.has("required_mods")) {
            JsonArray requiredMods = jsonObject.getAsJsonArray("required_mods");
            for (JsonElement requiredMod : requiredMods) {
                if (!isModPresent(requiredMod.getAsString())) {
                    LOGGER.error("Could not load plugin: %s. Client does not have required mod: %s".formatted(name, requiredMod.getAsString()));
                    return;
                }
            }
        }
        sanityCheckFiles(mainClass, jsonObject.get("mixins").getAsString(), jarFile);
        if (fileOfJar != null)
            FabricLauncherBase.getLauncher().addToClassPath(fileOfJar.toPath());
        JexPlugin jexPlugin = add(name, version, Class.forName(mainClass), description, toArray(list), allowDisable);
        if (jsonObject.has("mixins")) {
            String mixinsFile = jsonObject.get("mixins").getAsString();
            jexPlugin.setMixins(mixinsFile);
        }
    }

    private void sanityCheckFiles(String mainClass, String mixinsLocation, JarFile jarFile) {
        //plugin loaded from dev environment
        if (jarFile == null) {
            try {
                Class.forName(mainClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Main class %s not found! Plugin will not be loaded!".formatted(mainClass));
            }
            if (mixinsLocation != null && !mixinsLocation.isEmpty()) {
                if (FabricLauncherBase.getLauncher().getResourceAsStream(mixinsLocation) == null)
                    throw new RuntimeException("Mixins file %s not found! Plugin will not be loaded!".formatted(mixinsLocation));
            }
            return;
        }
        JarEntry mainClassEntry = jarFile.getJarEntry(mainClass);
        if (mainClassEntry == null)
            throw new RuntimeException("Main class %s not found! Plugin will not be loaded!".formatted(mainClass));
        if (mixinsLocation != null && !mixinsLocation.isEmpty()) {
            JarEntry mixinsEntry = jarFile.getJarEntry(mixinsLocation);
            if (mixinsEntry == null)
                throw new RuntimeException("Mixins file %s not found! Plugin will not be loaded!".formatted(mixinsLocation));
        }
    }

    //FabricLoaderImpl.getModContainer() was returning null for some reason so I made this workaround
    private boolean isModPresent(String modId) {
        for (ModContainer allMod : FabricLoaderImpl.INSTANCE.getAllMods()) {
            if (allMod.getMetadata().getId().equalsIgnoreCase(modId))
                return true;
        }
        return false;
    }

    private String[] toArray(ArrayList<String> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public JexPlugin get(String name) {
        for (JexPlugin plugin : plugins) {
            if (plugin.getName().equals(name))
                return plugin;
        }
        return null;
    }

    public ArrayList<JexPlugin> getPlugins() {
        return plugins;
    }
}
