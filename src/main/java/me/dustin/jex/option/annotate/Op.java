package me.dustin.jex.option.annotate;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Op {
    String name();

    float min() default 0;

    float max() default 1;

    float inc() default 1;

    boolean isColor() default false;

    int maxStringLength() default 5096;

    String[] all() default {};
}
