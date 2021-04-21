package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.client.options.KeyBinding;

@Feat(name = "AutoWalk", category = FeatureCategory.PLAYER, description = "Automatically hold W")
public class AutoWalk extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().keyForward.getDefaultKey(), true);
        }
    }

    @Override
    public void onDisable() {
        try {
            KeyBinding.setKeyPressed(Wrapper.INSTANCE.getOptions().keyForward.getDefaultKey(), false);
        } catch (NullPointerException e) {
        }
        super.onDisable();
    }
}
