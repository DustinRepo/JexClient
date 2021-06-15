package me.dustin.jex.feature.impl.render;

import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "CustomFont", category = FeatureCategory.VISUAL, description = "Change the font in aspects of the game")
public class CustomFont extends Feature {
    public static CustomFont INSTANCE;

    public CustomFont() {
        INSTANCE = this;
    }

}
