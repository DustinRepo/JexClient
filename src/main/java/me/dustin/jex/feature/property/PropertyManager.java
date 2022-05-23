package me.dustin.jex.feature.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum PropertyManager {
    INSTANCE;
    private final Map<Class<?>, ArrayList<Property<?>>> propertyMap = new HashMap<>();

    public void add(Class<?> clazz, Property<?> prop) {
        if (propertyMap.get(clazz) != null) {
            propertyMap.get(clazz).add(prop);
        } else {
            ArrayList<Property<?>> arrayList = new ArrayList<>();
            arrayList.add(prop);
            propertyMap.put(clazz, arrayList);
        }
    }

    public boolean hasProperties(Class<?> clazz) {
        return propertyMap.containsKey(clazz);
    }

    public ArrayList<Property<?>> get(Class<?> clazz) {
        return propertyMap.get(clazz);
    }

    public Property<?> get(Class<?> clazz, String name) {
        ArrayList<Property<?>> list = propertyMap.get(clazz);
        if (!hasProperties(clazz))
            return null;
        for (Property<?> property : list) {
            if (property.getName().equalsIgnoreCase(name))
                return property;
        }
        return null;
    }
}
