package me.dustin.jex.feature.option.types;


import me.dustin.jex.feature.option.Option;

import java.lang.reflect.Field;

public class ColorOption extends Option {

    public int h;
    public float s, b;

    public ColorOption(String name) {
        this.name = name;
    }

    @SuppressWarnings("deprecation")
	public int getValue() {
        try {
            for (Field f : this.getFeature().getClass().getDeclaredFields()) {
                if (!f.isAccessible())
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

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public float getS() {
        return s;
    }

    public void setS(float s) {
        this.s = s;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }
}
