package me.dustin.jex.event.render;

import me.dustin.events.core.Event;
import net.minecraft.client.render.CameraSubmersionType;

public class EventApplyFog extends Event {

    private CameraSubmersionType cameraSubmersionType;

    public EventApplyFog(CameraSubmersionType cameraSubmersionType) {
        this.cameraSubmersionType = cameraSubmersionType;
    }

    public CameraSubmersionType getCameraSubmersionType() {
        return cameraSubmersionType;
    }
}
