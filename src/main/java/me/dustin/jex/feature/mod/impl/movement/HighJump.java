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
.value(0)
.min(2)
.max(1000)
.inc(1)
.build();
	
public HighJump() {
super(Category.MOVEMENT, "Multiple the jump height");
 }
}	      
@Eventpointer
private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
Wrapper.INSTANCE.getLocalPlayer().jumplevelProperty.value();
event.setY((int)(event.getY() * jumplevelProperty.value));
}		 
