package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Open GUIs while in portals.")
public class PortalGui extends Feature {
    @EventPointer
    private final EventListener<EventPortalCloseGUI> eventPortalCloseGUIEventListener = new EventListener<>(event -> event.cancel());
}
