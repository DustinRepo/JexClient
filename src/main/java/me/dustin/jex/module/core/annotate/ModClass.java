package me.dustin.jex.module.core.annotate;

import me.dustin.jex.module.core.enums.ModCategory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModClass {
    String name();

    ModCategory category();

    String description();
}
