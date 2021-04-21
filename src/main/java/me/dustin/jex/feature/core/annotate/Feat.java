package me.dustin.jex.feature.core.annotate;

import me.dustin.jex.feature.core.enums.FeatureCategory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Feat {
    String name();

    FeatureCategory category();

    String description();
}
