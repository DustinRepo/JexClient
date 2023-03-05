package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.events.core.EventListener;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventWalkOffBlock;

public class ParkourR extends Feature {
	
private boolean walkoff = false;

    public ParkourR() {
        super(Category.MOVEMENT, "Recoded parkour");
    }

    @EventPointer
    private final EventListener<EventWalkOffBlock> eventWalkOffBlockEventListener = new EventListener<>(event -> {
	 boolean walkOff = walkoff;   
         walkoff = false;
         event.cancel();	    
         walkoff = true;    
	  if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && walkOff && PlayerHelper.INSTANCE.isMoving()) {
            Wrapper.INSTANCE.getLocalPlayer().jump();
           }
    });
}
