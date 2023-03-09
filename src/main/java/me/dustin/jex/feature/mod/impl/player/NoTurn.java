package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.misc.EventServerTurn;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;

public class NoTurn extends Feature {

    private boolean reconnected;

    public NoTurn() {
        super(Category.PLAYER);
    }

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
