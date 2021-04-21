package me.dustin.jex.option.types;


import me.dustin.jex.option.Option;

import java.lang.reflect.Field;


public class FloatOption extends Option {
    private float min, max, inc;
    private boolean isColor;

    public FloatOption(String name, float min, float max, float inc, boolean isColor) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.inc = inc;
        this.isColor = isColor;
    }

    public float getValue() {
        try {
            for (Field f : this.getFeature().getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getName().equalsIgnoreCase(this.getFieldName())) {
                    return f.getFloat(this.getFeature());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getInc() {
        return inc;
    }

    public void setInc(float inc) {
        this.inc = inc;
    }


    public boolean isColor() {
        return isColor;
    }
}
