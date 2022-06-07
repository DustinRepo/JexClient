package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.CameraSubmersionType;

public class EventSetupFog extends Event {

    private final CameraSubmersionType cameraSubmersionType;

    public EventSetupFog(CameraSubmersionType cameraSubmersionType) {
        this.cameraSubmersionType = cameraSubmersionType;
    }

    public CameraSubmersionType getCameraSubmersionType() {
        return cameraSubmersionType;
    }
}
