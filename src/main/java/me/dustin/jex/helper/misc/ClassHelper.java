package me.dustin.jex.helper.misc;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum ClassHelper {
    INSTANCE;
    @SuppressWarnings("UnstableApiUsage")
    public List<Class<?>> getClasses(String packageName, Class<?> assignableFrom) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ImmutableSet<ClassPath.ClassInfo> classInfoSet = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(packageName);
            classInfoSet.forEach(classInfo -> {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());
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
