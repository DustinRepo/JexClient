package me.dustin.jex.helper.file;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import me.dustin.jex.JexClient;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public enum ClassHelper {
    INSTANCE;

    public ArrayList<Class<?>> getClassesOther(String packageName, Class<?> assignableFrom) throws IOException, ClassNotFoundException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try {
                dirs.add(new File(resource.toURI()));
            } catch (IllegalArgumentException e) {
                dirs.add(new File(resource.getFile()));
            }
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName, assignableFrom));
        }
        ArrayList<Class<?>> removedDupes = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (!removedDupes.contains(clazz))
                removedDupes.add(clazz);
        }
        return removedDupes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName, Class<?> assignableFrom) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName(), assignableFrom));
            } else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (assignableFrom.isAssignableFrom(clazz))
                    classes.add(clazz);
            } else {
            }
        }
        return classes;
    }

    @SuppressWarnings("UnstableApiUsage")
    public List<Class<?>> getClasses(String packageName, Class<?> assignableFrom) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ImmutableSet<ClassPath.ClassInfo> classInfoSet = ClassPath.from(assignableFrom.getClassLoader()).getTopLevelClassesRecursive(packageName);
            JexClient.INSTANCE.getLogger().info("ClassInfo list size: " + classInfoSet.size());
            classInfoSet.forEach(classInfo -> {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());
                    JexClient.INSTANCE.getLogger().info(clazz.getSimpleName() + " " + assignableFrom.isAssignableFrom(clazz));
                    if(assignableFrom.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }
}
