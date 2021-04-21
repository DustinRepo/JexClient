package me.dustin.jex.option.types;


import me.dustin.jex.option.Option;

import java.lang.reflect.Field;


public class BoolOption extends Option {
    public BoolOption(String name, String fieldName) {
        this.name = name;
        this.setFieldName(fieldName);
    }

    public boolean getValue() {
        try {
            for (Field f : this.getFeature().getClass().getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(this.getFieldName())) {
                    return f.getBoolean(this.getFeature());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
