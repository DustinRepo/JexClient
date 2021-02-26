package me.dustin.jex.module.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "YawLock", category = ModCategory.MOVEMENT, description = "Keep your yaw locked to walk straight.")
public class YawLock extends Module {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            switch (Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing()) {
                case NORTH:
                    Wrapper.INSTANCE.getLocalPlayer().yaw = -180;
                    break;
                case SOUTH:
                    Wrapper.INSTANCE.getLocalPlayer().yaw = 0;
                    break;
                case EAST:
                    Wrapper.INSTANCE.getLocalPlayer().yaw = -90;
                    break;
                case WEST:
                    Wrapper.INSTANCE.getLocalPlayer().yaw = 90;
                    break;
            }
        }
    }

}
