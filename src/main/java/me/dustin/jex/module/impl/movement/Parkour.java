package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "Parkour", category = ModCategory.MOVEMENT, description = "Jump while on edge of block.")
public class Parkour extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && EntityHelper.INSTANCE.distanceFromGround(Wrapper.INSTANCE.getLocalPlayer()) > 0.5f && PlayerHelper.INSTANCE.isMoving()) {
                Wrapper.INSTANCE.getLocalPlayer().jump();
                Wrapper.INSTANCE.getLocalPlayer().getVelocity().multiply(1.2f, 1, 1.2f);
            }
        }
    }

}
