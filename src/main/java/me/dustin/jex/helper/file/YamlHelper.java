package me.dustin.jex.helper.file;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public enum YamlHelper {
    INSTANCE;

    private Yaml yaml;

    public void writeFile(Map<String, Object> yamlData, File file) {
        if (yaml == null)
            createYamlInstance();
        if (!file.exists())
            FileHelper.INSTANCE.createFile(file.getParentFile(), file.getName());
        try {
            PrintWriter printWriter = new PrintWriter(file);
            yaml.dump(yamlData, printWriter);
            printWriter.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public Map<String, Object> readFile(File file) {
        if (yaml == null)
            createYamlInstance();
        if (!file.exists())
            return new HashMap<>();
        try {
            InputStream inputStream = new FileInputStream(file);
            Map<String, Object> parsed = yaml.load(inputStream);
            inputStream.close();
            return parsed;
        } catch (Exception e) { e.printStackTrace(); }
        return new HashMap<>();
    }

    public void createYamlInstance() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndentWithIndicator(true);
        yaml = new Yaml(options);
    }
}
