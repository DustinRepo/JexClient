package me.dustin.jex.event.filters;

import me.dustin.jex.event.render.EventSetupFog;
import net.minecraft.world.level.material.FogType;
import java.util.function.Predicate;

public class ApplyFogFilter  implements Predicate<EventSetupFog> {

    private FogType[] cameraSubmersionTypes;

    public ApplyFogFilter(FogType... cameraSubmersionTypes) {
        this.cameraSubmersionTypes = cameraSubmersionTypes;
    }

    @Override
    public boolean test(EventSetupFog eventSetupFog) {
        if (cameraSubmersionTypes == null || cameraSubmersionTypes.length == 0)
            return true;
        for (FogType cameraSubmersionType : cameraSubmersionTypes) {
            if (cameraSubmersionType == eventSetupFog.getCameraSubmersionType())
                return true;
        }
        return false;
    }
}
