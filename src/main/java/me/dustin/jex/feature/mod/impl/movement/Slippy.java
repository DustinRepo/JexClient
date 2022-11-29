package me.dustin.jex.feature.mod.impl.movement;

import net.minecraft.block.Block;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.block.AbstractBlock;

public class Slippy extends Feature {
 
 public Slippy() {
   super(Category.MOVEMENT, "Сhanges the block to ice for fast movement");
    }
 
 @EventPointer
 public final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
 if (Wrapper.INSTANCE.getLocalPlayer().isOnGround()){
  Block block;
  this.slipperiness = 0.98F;
    }
  });
}
