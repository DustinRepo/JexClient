package me.dustin.jex.feature.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.misc.Wrapper;

@Feat(name = "AutoMine", category = FeatureCategory.WORLD, description = "Automatically mine any block you hover over.")
public class AutoMine extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            Wrapper.INSTANCE.getOptions().keyAttack.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        Wrapper.INSTANCE.getOptions().keyAttack.setPressed(false);
        super.onDisable();
    }
}
