package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.world.level.material.FogType;

public class EventSetupFog extends Event {

    private final FogType cameraSubmersionType;

    public EventSetupFog(FogType cameraSubmersionType) {
        this.cameraSubmersionType = cameraSubmersionType;
    }

    public FogType getCameraSubmersionType() {
        return cameraSubmersionType;
    }
}
