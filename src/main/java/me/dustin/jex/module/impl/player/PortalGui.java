package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "PortalGui", category = ModCategory.PLAYER, description = "Open GUIs while in portals.")
public class PortalGui extends Module {

    @EventListener(events = {EventPortalCloseGUI.class})
    public void run(EventPortalCloseGUI event) {
        event.cancel();
    }

}
