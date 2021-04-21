package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventPortalCloseGUI;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "PortalGui", category = FeatureCategory.PLAYER, description = "Open GUIs while in portals.")
public class PortalGui extends Feature {

    @EventListener(events = {EventPortalCloseGUI.class})
    public void run(EventPortalCloseGUI event) {
        event.cancel();
    }

}
