package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(name = "PortalGui", category = Feature.Category.PLAYER, description = "Open GUIs while in portals.")
public class PortalGui extends Feature {

    @EventListener(events = {EventPortalCloseGUI.class})
    public void run(EventPortalCloseGUI event) {
        event.cancel();
    }

}
