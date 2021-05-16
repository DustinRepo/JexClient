package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.misc.EventServerTurn;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "NoTurn", category = FeatureCategory.PLAYER, description = "Ignore the server telling you to look somewhere.")
public class NoTurn extends Feature {

    boolean reconnected;

    @EventListener(events = {EventServerTurn.class, EventJoinWorld.class})
    public void runEvent(Event event) {
        if (event.equals(EventServerTurn.class)) {
            if (reconnected) {
                reconnected = false;
                return;
            }
            event.cancel();
        }
        if (event.equals(EventJoinWorld.class)) {
            reconnected = true;
        }
    }

}
