package me.dustin.jex.feature.option;

import me.dustin.jex.JexClient;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.enums.OpType;
import me.dustin.jex.feature.option.types.*;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Option {
    protected String name;
    protected Field field;
    protected String fieldName;
    protected Feature feature;
    protected OpType type;
    protected Option parent;
    protected String dependency;
    protected ArrayList<Option> children = new ArrayList<>();
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public OpType getType() {
        return type;
    }

    public void setType(OpType type) {
        this.type = type;
    }

    public Feature getFeature() {
        return feature;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Option getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasChild() {
        return !children.isEmpty();
    }

    public ArrayList<Option> getChildren() {
        return children;
    }

    public String getDependency() {
        return dependency;
    }

    public boolean hasDependency() {
        return !dependency.isEmpty();
    }

    public Object value() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        for (Field f : this.getFeature().getClass().getDeclaredFields()) {
            if (f.getName().equalsIgnoreCase(this.getFieldName())) {
                try {
                    f.set(this.getFeature(), value);
                } catch (Exception e) {
                }
            }
        }
    }

    public void parseValue(String value) {
        try {
            if (this instanceof StringOption)
                this.setValue(value);
            if (this instanceof StringArrayOption castOption) {
                if (contains(castOption.getAll(), value)) {
                    Timer timer = new Timer();
                    while (!castOption.getValue().equalsIgnoreCase(value)) {
                        castOption.inc();
                        if (timer.hasPassed(1000)) {
                            castOption.setValue(value);
                            break;
                        }
                    }
                }
            }
            if (this instanceof ColorOption colorOption) {
                Color color = Render2DHelper.INSTANCE.hex2Rgb(value);
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

                colorOption.setH((int) (hsb[0] * 270));
                colorOption.setS(hsb[1]);
                colorOption.setB(hsb[2]);

                colorOption.setValue(color.getRGB());
            }
            if (this instanceof IntOption || this instanceof KeybindOption) {
                this.setValue(Integer.valueOf(value));
            }
            if (this instanceof FloatOption) {
                this.setValue(Float.valueOf(value));
            }
            if (this instanceof BoolOption) {
                this.setValue(Boolean.valueOf(value));
            }
        }catch (Exception e) {
            e.printStackTrace();
            JexClient.INSTANCE.getLogger().error("Caused by " + this.feature.getName() + " " + this.getName());
        }
    }
    private boolean contains(String[] values, String value)
    {
        for(String s : values)
        {
            if(s.equalsIgnoreCase(value))
                return true;
        }
        return false;
    }
}
