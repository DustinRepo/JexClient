package me.dustin.jex.feature.mod.impl.movement.speed;

import net.minecraft.block.Block;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;

public class Slippy extends Feature {

 public Slippy() {
        super(Category.MOVEMENT, "Сhanges the block to ice for fast movement");
    }

public final Property<Float> friction = new Property.PropertyBuilder<Float>(this.getClass())
.name("Friction")
.description("The base friction level.")
.value(0.01f)
.min(0.01f)
.max(1.10f)
.inc(0.01f)
.build();

}
