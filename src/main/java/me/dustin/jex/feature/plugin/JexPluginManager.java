package me.dustin.jex.feature.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
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

    public JexPlugin add(String name, Class<?> clazz, String description, String[] authors, boolean allowDisable) {
        JexPlugin jexPlugin = new JexPlugin(name, clazz, description, authors, allowDisable);
        JexPluginManager.INSTANCE.getPlugins().add(jexPlugin);
        return jexPlugin;
    }
    public void loadPlugins() {
        File pluginsFolder = new File(ModFileHelper.INSTANCE.getJexDirectory(), "plugins");
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
            return;
        }
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("JexPlugin.json");
            if (inputStream != null) {
                JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(read(inputStream), JsonObject.class);
                loadFromJson(jsonObject);
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
                            FabricLauncherBase.getLauncher().addToClassPath(file.toPath());
                            InputStream inputStream = jarFile.getInputStream(entry);

                            JsonObject jsonObject = JsonHelper.INSTANCE.gson.fromJson(read(inputStream), JsonObject.class);
                            loadFromJson(jsonObject);
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

    private void loadFromJson(JsonObject jsonObject) throws ClassNotFoundException {
        String name = jsonObject.get("name").getAsString();
        String mainClass = jsonObject.get("mainClass").getAsString();
        String description = jsonObject.get("description").getAsString();
        ArrayList<String> list = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("authors");
        jsonArray.forEach(jsonElement -> list.add(jsonElement.getAsString()));
        boolean allowDisable = jsonObject.get("allowDisable").getAsBoolean();

        LOGGER.info("Found Plugin: %s".formatted(name));
        JexPlugin jexPlugin = add(name, Class.forName(mainClass), description, toArray(list), allowDisable);
        if (jsonObject.has("mixins")) {
            String mixinsFile = jsonObject.get("mixins").getAsString();
            jexPlugin.setMixins(mixinsFile);
        }
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
