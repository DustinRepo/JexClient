package me.dustin.jex.event.filters;

import me.dustin.jex.event.render.EventSetupFog;
import net.minecraft.client.render.CameraSubmersionType;
import java.util.function.Predicate;

public class ApplyFogFilter  implements Predicate<EventSetupFog> {

    private CameraSubmersionType[] cameraSubmersionTypes;

    public ApplyFogFilter(CameraSubmersionType... cameraSubmersionTypes) {
        this.cameraSubmersionTypes = cameraSubmersionTypes;
    }

    @Override
    public boolean test(EventSetupFog eventSetupFog) {
        if (cameraSubmersionTypes == null || cameraSubmersionTypes.length == 0)
            return true;
        for (CameraSubmersionType cameraSubmersionType : cameraSubmersionTypes) {
            if (cameraSubmersionType == eventSetupFog.getCameraSubmersionType())
                return true;
        }
        return false;
    }
}
