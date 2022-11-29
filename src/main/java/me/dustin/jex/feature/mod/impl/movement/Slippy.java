package me.dustin.jex.feature.mod.impl.movement;

import net.minecraft.block.Block;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.events.core.EventListener;

public class Slippy extends Feature {

public final Property<Float> friction = new Property.PropertyBuilder<Float>(this.getClass())
.name("Friction")
.description("The base friction level.")
.value(0.01f)
.min(0.01f)
.max(1.10f)
.inc(0.01f)
.build();
 
 public Slippy() {
   super(Category.MOVEMENT, "Сhanges the block to ice for fast movement");
    }
 
 private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
 if (Wrapper.INSTANCE.getBlock().getLocalPlayer().isOnGround(event.getBlockPos()) && event.getVoxelShape().isEmpty){
  Block block;
  block.slipperiness = friction.value();
 }
});
 
 
 
 }
