package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "AntiHunger", category = ModCategory.PLAYER, description = "Lose less hunger while sprinting.")
public class AntiHunger extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            if (Wrapper.INSTANCE.getLocalPlayer() == null)
                return;
            if (Wrapper.INSTANCE.getLocalPlayer().prevY == Wrapper.INSTANCE.getLocalPlayer().getY())
                event.setOnGround(false);
        }
    }

}
