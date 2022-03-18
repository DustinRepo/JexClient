package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventExplosionVelocity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventPlayerVelocity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Remove all knockback from the player.")
public class AntiKnockback extends Feature {

    @Op(name = "Percent", min = -300, max = 300, inc = 10)
    public int percent = 0;

    @EventPointer
    private final EventListener<EventExplosionVelocity> eventExplosionVelocityEventListener = new EventListener<>(event -> {
        float perc = percent / 100.0f;
        if (percent == 0)
            event.cancel();
        else {
            event.setMultX(perc);
            event.setMultY(perc);
            event.setMultZ(perc);
        }
    });

    @EventPointer
    private final EventListener<EventPlayerVelocity> eventPlayerVelocityEventListener = new EventListener<>(event -> {
        float perc = percent / 100.0f;
        if (percent == 0)
            event.cancel();
        else {
            event.setVelocityX((int)(event.getVelocityX() * perc));
            event.setVelocityY((int)(event.getVelocityY() * perc));
            event.setVelocityZ((int)(event.getVelocityZ() * perc));
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        this.setSuffix(percent + "%");
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
