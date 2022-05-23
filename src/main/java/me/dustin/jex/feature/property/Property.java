package me.dustin.jex.feature.property;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Property<T> {
    private String name;
    private String description;
    private T value;
    private T defaultValue;

    private Property<?> parent;
    private Predicate<Property<?>> dependsPredicate;

    private float min = 0, max = 1, inc = 1;
    private boolean isKeybind;

    private final ArrayList<Property> children = new ArrayList<>();

    private Property() {

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T value() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getInc() {
        return inc;
    }

    public boolean isKeybind() {
        return isKeybind;
    }

    public Property<?> getParent() {
        return parent;
    }

    public Predicate<Property<?>> getDepends() {
        return dependsPredicate;
    }

    public ArrayList<Property> getChildren() {
        return children;
    }

    public boolean passes() {
        if (dependsPredicate == null)
            return true;
        return dependsPredicate.test(this.parent);
    }

    public void incrementEnumValue() {
        Object[] values = value.getClass().getEnumConstants();
        int i = 0;
        for (Object o : values) {
            i++;
            if (o.toString().equalsIgnoreCase(value.toString()))
                break;
        }
        if (i > values.length - 1)
            i = 0;
        value = (T) values[i];
    }

    public void setEnumValue(String name) {
        Object[] values = value.getClass().getEnumConstants();
        for (Object o : values) {
            if (o.toString().equalsIgnoreCase(name)) {
                value = (T) o;
                return;
            }
        }
    }

    public static class PropertyBuilder<T> {

        private final Property<T> property;
        private final Class<?> clazz;

        public PropertyBuilder(Class<?> clazz) {
            this.clazz = clazz;
            this.property = new Property<>();
        }

        public PropertyBuilder<T> name(String name) {
            property.name = name;
            return this;
        }

        public PropertyBuilder<T> description(String description) {
            property.description = description;
            return this;
        }

        public PropertyBuilder<T> value(T value) {
            property.value = value;
            property.defaultValue = value;
            return this;
        }

        public PropertyBuilder<T> min(float min) {
            property.min = min;
            return this;
        }

        public PropertyBuilder<T> max(float max) {
            property.max = max;
            return this;
        }

        public PropertyBuilder<T> inc(float inc) {
            property.inc = inc;
            return this;
        }

        public PropertyBuilder<T> isKey() {
            property.isKeybind = true;
            return this;
        }

        public PropertyBuilder<T> parent(Property<?> parent) {
            property.parent = parent;
            parent.children.add(property);
            return this;
        }

        public PropertyBuilder<T> depends(Predicate<Property<?>> booleanPredicate) {
            property.dependsPredicate = booleanPredicate;
            return this;
        }

        public Property<T> build() {
            if (property.name == null || property.value == null)
                throw new RuntimeException("You must specify atleast a name and value!");
            PropertyManager.INSTANCE.add(clazz, property);
            return property;
        }
    }
}
