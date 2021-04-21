package me.dustin.jex.option.types;


import me.dustin.jex.option.Option;

import java.lang.reflect.Field;

public class IntOption extends Option {


    private int min, max, inc;

    public IntOption(String name, int min, int max, int inc) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.inc = inc;
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

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getInc() {
        return inc;
    }

    public void setInc(int inc) {
        this.inc = inc;
    }
}
