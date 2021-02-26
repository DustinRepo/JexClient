package me.dustin.jex.module.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "AutoRespawn", category = ModCategory.MISC, description = "Respawn without having to click anything.")
public class AutoRespawn extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets eventPlayerPackets) {
        if (!Wrapper.INSTANCE.getLocalPlayer().isAlive())
            Wrapper.INSTANCE.getLocalPlayer().requestRespawn();
    }

}
