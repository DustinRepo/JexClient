package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "YawLock", category = FeatureCategory.MOVEMENT, description = "Keep your yaw locked to walk straight.")
public class YawLock extends Feature {

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
