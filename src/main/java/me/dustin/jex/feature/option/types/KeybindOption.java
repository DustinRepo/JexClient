package me.dustin.jex.feature.option.types;


import me.dustin.jex.feature.option.Option;

import java.lang.reflect.Field;

public class KeybindOption extends Option {

    public KeybindOption(String name) {
        this.name = name;
    }

    public int getValue() {
        try {
            for (Field f : this.getFeature().getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getName().equalsIgnoreCase(this.getFieldName())) {
                    return f.getInt(this.getFeature());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
