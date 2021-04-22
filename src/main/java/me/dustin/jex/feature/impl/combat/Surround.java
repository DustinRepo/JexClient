package me.dustin.jex.feature.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;

@Feat(name = "Surround", category = FeatureCategory.COMBAT, description = "Automatically place obsidian around your feet to defend from crystals")
public class Surround extends Feature {

    @Op(name = "Place Delay (MS)", min = 0, max = 250)
    public int placeDelay = 0;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {

        }
    }

}
