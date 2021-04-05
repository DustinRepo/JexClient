package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.client.option.KeyBinding;

@ModClass(name = "AutoWalk", category = ModCategory.PLAYER, description = "Automatically hold W")
public class AutoWalk extends Module {

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
