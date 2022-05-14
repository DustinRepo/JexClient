package me.dustin.jex.helper.misc;

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

    @SuppressWarnings("UnstableApiUsage")
    public List<Class<?>> getClasses(String packageName, Class<?> assignableFrom) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ImmutableSet<ClassPath.ClassInfo> classInfoSet = ClassPath.from(assignableFrom.getClassLoader()).getTopLevelClassesRecursive(packageName);
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

    @SuppressWarnings("UnstableApiUsage")
    public List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ImmutableSet<ClassPath.ClassInfo> classInfoSet = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive(packageName);
            classInfoSet.forEach(classInfo -> {
                try {
                    Class<?> clazz = Class.forName(classInfo.getName());
                    classes.add(clazz);
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
