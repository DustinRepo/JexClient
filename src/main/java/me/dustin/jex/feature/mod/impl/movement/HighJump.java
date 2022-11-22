package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import me.dustin.jex.feature.property.Property;

public class HighJump extends Feature {
  	
public Property <Integer> jumplevelProperty = new Property.PropertyBuilder<Integer>(this.getClass())
.name ("Jump level")
.value(1)
.min(1)
.max(256)
.inc(1)
.build();
	
public HighJump() {
super(Category.MOVEMENT, "Multiple the jump height");
 }
	      
@EventPointer
public final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
event.setX(event.getX() * 1.72111554);	
event.setY(event.getY() * jumplevelProperty.value());
event.setZ(event.getZ() * 1.72111554);
   });	
}
