package me.dustin.jex.option.types;


import me.dustin.jex.option.Option;

import java.lang.reflect.Field;

public class StringArrayOption extends Option {
    private String[] all;
    private int counter;

    public StringArrayOption(String name, String[] all) {
        this.name = name;
        this.all = all;
    }

    public void inc() {
        counter++;
        if (counter > all.length - 1)
            counter = 0;
        setValue(all[counter]);
    }

    public String getValue() {
        try {
            for (Field f : this.getModule().getClass().getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(this.getFieldName())) {
                    return (String) f.get(this.getModule());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "broke";
    }

    public String[] getAll() {
        return all;
    }

}
