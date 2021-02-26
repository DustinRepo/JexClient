package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventSetSprint;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;

@ModClass(name = "Sprint", category = ModCategory.MOVEMENT, description = "Automatically sprint")
public class Sprint extends Module {

    @Op(name = "Multi Dir")
    public boolean multiDir;

    @EventListener(events = {EventPlayerPackets.class, EventSetSprint.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets) {
            Wrapper.INSTANCE.getLocalPlayer().setSprinting(canSprint());
        } else if (event instanceof EventSetSprint) {
            if (!((EventSetSprint) event).isSprint() && canSprint()) {
                ((EventSetSprint) event).setSprint(true);
            }
        }
    }

    private boolean canSprint() {
        return isMoving() && !Wrapper.INSTANCE.getLocalPlayer().horizontalCollision && Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() > 6;
    }

    private boolean isMoving() {
        if (multiDir) {
            return PlayerHelper.INSTANCE.isMoving();
        } else {
            return Wrapper.INSTANCE.getLocalPlayer().input.movementForward == 1;
        }
    }
}
