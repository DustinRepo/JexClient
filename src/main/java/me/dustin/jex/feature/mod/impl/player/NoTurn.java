package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.misc.EventServerTurn;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Ignore the server telling you to look somewhere.")
public class NoTurn extends Feature {

    boolean reconnected;

    @EventPointer
    private final EventListener<EventServerTurn> eventServerTurnEventListener = new EventListener<>(event -> {
        if (reconnected) {
            reconnected = false;
            return;
        }
        event.cancel();
    });

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        reconnected = true;
    });
}
