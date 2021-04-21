package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventHurtCam;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "NoHurtCam", category = FeatureCategory.VISUAL, description = "Remove the hurt-cam effect that bobs your view when damaged.")
public class NoHurtCam extends Feature {

    @EventListener(events = {EventHurtCam.class})
    private void runMethod(EventHurtCam eventHurtCam) {
        eventHurtCam.cancel();
    }

}
