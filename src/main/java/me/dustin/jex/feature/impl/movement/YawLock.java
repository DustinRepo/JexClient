package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.helper.player.PlayerHelper;

@Feature.Manifest(name = "YawLock", category = Feature.Category.MOVEMENT, description = "Keep your yaw locked to walk straight.")
public class YawLock extends Feature {

    @EventListener(events = {EventPlayerPackets.class})
    public void run(EventPlayerPackets event) {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            switch (Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing()) {
                case NORTH -> PlayerHelper.INSTANCE.setYaw(-180);
                case SOUTH -> PlayerHelper.INSTANCE.setYaw(0);
                case EAST -> PlayerHelper.INSTANCE.setYaw(-90);
                case WEST -> PlayerHelper.INSTANCE.setYaw(90);
            }
        }
    }

}
