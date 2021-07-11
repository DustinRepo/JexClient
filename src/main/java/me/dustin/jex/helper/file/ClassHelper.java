package me.dustin.jex.helper.file;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import me.dustin.jex.JexClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum ClassHelper {
    INSTANCE;

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
