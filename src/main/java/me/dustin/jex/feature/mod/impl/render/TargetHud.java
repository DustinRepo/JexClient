package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;

import java.awt.*;

public class TargetHud extends Feature {
    public TargetHud() {
        super(Category.VISUAL);
    }

    public final Property<Boolean> markTargetProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Mark Target")
            .value(true)
            .build();
    public final Property<Color> markColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
            .name("Color")
            .value(new Color(0, 180, 255))
            .parent(markTargetProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Integer> stopTargetDistanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Stop Targeting Distance")
            .value(15)
            .min(10)
            .max(100)
            .build();
}
