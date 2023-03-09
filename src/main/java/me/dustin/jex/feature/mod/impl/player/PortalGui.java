package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;

public class PortalGui extends Feature {

    public PortalGui() {
        super(Category.PLAYER);
    }

    @EventPointer
    private final EventListener<EventPortalCloseGUI> eventPortalCloseGUIEventListener = new EventListener<>(event -> event.cancel());
}
