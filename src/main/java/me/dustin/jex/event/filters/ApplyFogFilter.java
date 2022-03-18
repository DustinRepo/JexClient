package me.dustin.jex.event.filters;

import me.dustin.jex.event.render.EventApplyFog;
import net.minecraft.client.render.CameraSubmersionType;

import java.util.function.Predicate;

public class ApplyFogFilter  implements Predicate<EventApplyFog> {

    private CameraSubmersionType[] cameraSubmersionTypes;

    public ApplyFogFilter(CameraSubmersionType... cameraSubmersionTypes) {
        this.cameraSubmersionTypes = cameraSubmersionTypes;
    }

    @Override
    public boolean test(EventApplyFog eventApplyFog) {
        if (cameraSubmersionTypes == null || cameraSubmersionTypes.length == 0)
            return true;
        for (CameraSubmersionType cameraSubmersionType : cameraSubmersionTypes) {
            if (cameraSubmersionType == eventApplyFog.getCameraSubmersionType())
                return true;
        }
        return false;
    }
}
