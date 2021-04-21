package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventClipCamera;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;

@Feat(name = "CameraClip", category = FeatureCategory.VISUAL, description = "Remove the restriction forcing cameras close near a wall")
public class CameraClip extends Feature {

    @EventListener(events = {EventClipCamera.class})
    private void runMethod(EventClipCamera eventClipCamera) {
        eventClipCamera.cancel();
    }

}
