package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventSetSprint;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.option.annotate.Op;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(name = "Sprint", category = Feature.Category.MOVEMENT, description = "Automatically sprint", key = GLFW.GLFW_KEY_V)
public class Sprint extends Feature {

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
