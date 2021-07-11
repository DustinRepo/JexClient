package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.option.KeyBinding;

@Feature.Manifest(name = "AutoWalk", category = Feature.Category.PLAYER, description = "Automatically hold W")
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
        } catch (NullPointerException ignored) {
        }
        super.onDisable();
    }
}
