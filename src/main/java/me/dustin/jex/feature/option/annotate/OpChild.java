package me.dustin.jex.feature.option.annotate;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface OpChild {
    String name();
    String parent();
    float min() default 0;
    float max() default 1;
    float inc() default 1;
    boolean isColor() default false;
    boolean isKeybind() default false;
    String[] all() default {};
    String dependency() default "";
}
