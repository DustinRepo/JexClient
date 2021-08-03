package me.dustin.jex.feature.option;

import com.google.common.collect.Maps;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.FeatureManager;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.feature.option.enums.OpType;
import me.dustin.jex.feature.option.types.*;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentMap;

public enum OptionManager {
    INSTANCE;

    private ArrayList<Option> options = new ArrayList<Option>();

    public static OptionManager get() {
        return INSTANCE;
    }

    @SuppressWarnings("deprecation")
	public void initializeOptionManager() {
        ConcurrentMap<Field, Feature> parentNotFound = Maps.newConcurrentMap();
        FeatureManager.INSTANCE.getFeatures().forEach(mod ->
        {
            for (Field field : mod.getClass().getDeclaredFields()) {
                if (!field.isAccessible())
                    field.setAccessible(true);
                if (field.isAnnotationPresent(Op.class)) {
                    if (field.getType() == float.class) {
                        try {
                            Op anot = field.getAnnotation(Op.class);
                            String name = anot.name();
                            float min = anot.min();
                            float max = anot.max();
                            float inc = anot.inc();
                            boolean isColor = anot.isColor();
                            OpType type = OpType.FLOAT;
                            FloatOption floatOption = new FloatOption(name, min, max, inc, isColor);
                            floatOption.type = type;
                            floatOption.field = field;
                            floatOption.fieldName = field.getName();
                            floatOption.feature = mod;

                            this.getOptions().add(floatOption);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                    if (field.getType() == int.class) {
                        try {
                            Op anot = field.getAnnotation(Op.class);
                            String name = anot.name();
                            int min = (int) anot.min();
                            int max = (int) anot.max();
                            int inc = (int) anot.inc();
                            boolean isColor = anot.isColor();
                            if (isColor) {
                                OpType type = OpType.COLOR;
                                ColorOption intOption = new ColorOption(name);
                                intOption.type = type;
                                intOption.field = field;
                                intOption.fieldName = field.getName();
                                intOption.feature = mod;
                                Color color = Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(intOption.getValue()));
                                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

                                intOption.setH((int) (hsb[0] * 270));
                                intOption.setS(hsb[1]);
                                intOption.setB(hsb[2]);

                                intOption.setValue(color.getRGB());
                                this.getOptions().add(intOption);
                            } else {
                                OpType type = OpType.INT;
                                IntOption intOption = new IntOption(name, min, max, inc);
                                intOption.type = type;
                                intOption.field = field;
                                intOption.fieldName = field.getName();
                                intOption.feature = mod;
                                this.getOptions().add(intOption);
                            }

                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                    }
                    if (field.getType().toString().equalsIgnoreCase("class java.lang.String")) {
                        try {
                            Op anot = field.getAnnotation(Op.class);
                            String name = anot.name();
                            if (anot.all().length > 0) {
                                OpType type = OpType.STRINGARRAY;
                                String[] all = anot.all();
                                StringArrayOption stringArrayOption = new StringArrayOption(name, all);
                                stringArrayOption.type = type;
                                stringArrayOption.fieldName = field.getName();
                                stringArrayOption.feature = mod;

                                this.getOptions().add(stringArrayOption);
                            } else {

                                OpType type = OpType.STRING;
                                StringOption stringOption = new StringOption(name);
                                stringOption.field = field;
                                stringOption.type = type;
                                stringOption.feature = mod;
                                stringOption.fieldName = field.getName();

                                this.getOptions().add(stringOption);
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                    }
                    if (field.getType() == boolean.class) {
                        try {
                            Op anot = field.getAnnotation(Op.class);
                            String name = anot.name();
                            OpType type = OpType.BOOL;
                            BoolOption option = new BoolOption(name, field.getName());
                            option.field = field;
                            option.setFieldName(field.getName());
                            option.type = type;
                            option.feature = mod;

                            this.getOptions().add(option);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            for (Field field : mod.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(OpChild.class))
                    if (!loadChildren(mod, field)) {
                        parentNotFound.put(field, mod);
                    }
            }
            int count = 0;
            while (!parentNotFound.isEmpty() && count < 15) {
                parentNotFound.keySet().forEach(field -> {
                    if (loadChildren(parentNotFound.get(field), field)) {
                        parentNotFound.remove(field);
                    } else {
                        //Protocol.getLogger().info("Couldn't parent for " + field.getName() + " " + finalCount);
                    }
                });
                count++;
            }
        });
        reorder();
    }

    public boolean loadChildren(Feature mod, Field field) {
        if (field.isAnnotationPresent(OpChild.class)) {
            if (field.getType() == float.class) {
                try {
                    OpChild anot = field.getAnnotation(OpChild.class);
                    String name = anot.name();
                    float min = anot.min();
                    float max = anot.max();
                    float inc = anot.inc();
                    boolean isColor = anot.isColor();
                    OpType type = OpType.FLOAT;
                    FloatOption floatOption = new FloatOption(name, min, max, inc, isColor);
                    floatOption.type = type;
                    floatOption.field = field;
                    floatOption.fieldName = field.getName();
                    floatOption.feature = mod;
                    floatOption.dependency = anot.dependency();
                    if (getOption(anot.parent(), mod) != null) {
                        floatOption.parent = getOption(anot.parent(), mod);
                        getOption(anot.parent(), mod).getChildren().add(floatOption);
                        this.getOptions().add(floatOption);
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            if (field.getType() == int.class) {
                try {


                    OpChild anot = field.getAnnotation(OpChild.class);
                    boolean isColor = anot.isColor();
                    String name = anot.name();
                    if (isColor) {
                        OpType type = OpType.COLOR;
                        ColorOption intOption = new ColorOption(name);
                        intOption.type = type;
                        intOption.field = field;
                        intOption.fieldName = field.getName();
                        intOption.feature = mod;
                        intOption.dependency = anot.dependency();
                        Color color = Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(intOption.getValue()));
                        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

                        intOption.setH((int) (hsb[0] * 270));
                        intOption.setS(hsb[1]);
                        intOption.setB(hsb[2]);

                        intOption.setValue(color.getRGB());

                        if (getOption(anot.parent(), mod) != null) {
                            intOption.parent = getOption(anot.parent(), mod);
                            getOption(anot.parent(), mod).getChildren().add(intOption);
                            this.getOptions().add(intOption);
                            return true;
                        }
                    } else {
                        int min = (int) anot.min();
                        int max = (int) anot.max();
                        int inc = (int) anot.inc();
                        OpType type = OpType.INT;
                        IntOption intOption = new IntOption(name, min, max, inc);
                        intOption.type = type;
                        intOption.field = field;
                        intOption.fieldName = field.getName();
                        intOption.feature = mod;
                        intOption.dependency = anot.dependency();

                        if (getOption(anot.parent(), mod) != null) {
                            intOption.parent = getOption(anot.parent(), mod);
                            getOption(anot.parent(), mod).getChildren().add(intOption);
                            this.getOptions().add(intOption);
                            return true;
                        }
                    }


                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }
            if (field.getType().toString().equalsIgnoreCase("class java.lang.String")) {
                try {
                    OpChild anot = field.getAnnotation(OpChild.class);
                    String name = anot.name();
                    if (anot.all().length > 0) {
                        OpType type = OpType.STRINGARRAY;
                        String[] all = anot.all();
                        StringArrayOption stringArrayOption = new StringArrayOption(name, all);
                        stringArrayOption.type = type;
                        stringArrayOption.fieldName = field.getName();
                        stringArrayOption.feature = mod;
                        stringArrayOption.dependency = anot.dependency();

                        if (getOption(anot.parent(), mod) != null) {
                            stringArrayOption.parent = getOption(anot.parent(), mod);
                            getOption(anot.parent(), mod).getChildren().add(stringArrayOption);
                            this.getOptions().add(stringArrayOption);
                            return true;
                        }
                    } else {

                        OpType type = OpType.STRING;
                        StringOption stringOption = new StringOption(name);
                        stringOption.field = field;
                        stringOption.type = type;
                        stringOption.feature = mod;
                        stringOption.fieldName = field.getName();
                        stringOption.dependency = anot.dependency();
                        if (!anot.parent().equals("") && getOption(anot.parent(), mod) != null) {
                            stringOption.parent = getOption(anot.parent(), mod);
                            getOption(anot.parent(), mod).getChildren().add(stringOption);
                            this.getOptions().add(stringOption);
                            return true;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }
            if (field.getType() == boolean.class) {
                try {
                    OpChild anot = field.getAnnotation(OpChild.class);
                    String name = anot.name();
                    OpType type = OpType.BOOL;
                    BoolOption option = new BoolOption(name, field.getName());
                    option.field = field;
                    option.setFieldName(field.getName());
                    option.type = type;
                    option.feature = mod;
                    option.dependency = anot.dependency();
                    if (getOption(anot.parent(), mod) != null) {
                        option.parent = getOption(anot.parent(), mod);
                        option.parent.getChildren().add(option);
                        this.getOptions().add(option);
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

            }
        }
        return false;
    }

    public void reorder() {
        ArrayList<Option> newList = (ArrayList<Option>) options;

        Collections.sort(newList, new Comparator<Option>() {

            public int compare(Option op1, Option op2) {
                if (op1.getType().ordinal() > op2.getType().ordinal()) {
                    return 1;
                }
                if (op1.getType().ordinal() < op2.getType().ordinal()) {
                    return -1;
                }
                return 0;
            }
        });
        options = newList;
    }

    public boolean hasOption(Feature mod) {
        return !getOptions(mod).isEmpty();
    }

    public Option getOption(String name) {
        for (Option o : OptionManager.get().getOptions()) {
            if (o.getName().equalsIgnoreCase(name))
                return o;
        }
        return null;
    }

    public Option getOption(Field f) {
        for (Option o : getOptions()) {
            if (o.getName().equalsIgnoreCase(f.getName())) {
                return o;
            }
        }
        return null;
    }

    public Option getOption(String name, Feature mod) {
        for (Option o : OptionManager.get().getOptions()) {
            if (o.getName().equalsIgnoreCase(name) && o.getFeature() == mod)
                return o;
        }
        return null;
    }

    public ArrayList<Option> getOptions(Feature mod) {
        ArrayList<Option> options = new ArrayList<>();
        getOptions().forEach(option -> {
            if (option.getFeature() == mod)
                options.add(option);
        });
        return options;
    }

    public ArrayList<Option> getOptionsNoChild(Feature mod) {
        ArrayList<Option> options = new ArrayList<>();
        getOptions().forEach(option -> {
            if (option.getFeature() == mod && !option.hasParent())
                options.add(option);
        });
        return options;
    }

    public ArrayList<Option> getOptions() {
        return options;
    }
}
