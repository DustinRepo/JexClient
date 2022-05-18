package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.player.PlayerHelper;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Keep your yaw locked to walk straight.")
public class YawLock extends Feature {

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        switch (Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing()) {
            case NORTH -> PlayerHelper.INSTANCE.setYaw(-180);
            case SOUTH -> PlayerHelper.INSTANCE.setYaw(0);
            case EAST -> PlayerHelper.INSTANCE.setYaw(-90);
            case WEST -> PlayerHelper.INSTANCE.setYaw(90);
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
