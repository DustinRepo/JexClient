package me.dustin.jex.module.impl.player;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.load.impl.IMinecraft;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "FastPlace", category = ModCategory.PLAYER, description = "Place fast.")
public class FastPlace extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setRightClickDelayTimer(0);
        }
    }

    @Override
    public void onDisable() {
        ((IMinecraft) Wrapper.INSTANCE.getMinecraft()).setRightClickDelayTimer(4);
        super.onDisable();
    }

}
