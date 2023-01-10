package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.util.Hand;

public class AttackClicker extends Feature {

    public AttackClicker() {
        super(Category.COMBAT, "Automatically click attack button, useful for pvp.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
             boolean attack = Wrapper.INSTANCE.getOptions().attackKey.isPressed();
            if (attack && stopWatch.hasPassed(1000)) {
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                stopWatch.reset();
            }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
