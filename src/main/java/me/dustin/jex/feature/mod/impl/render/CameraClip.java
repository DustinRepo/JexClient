package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventClipCamera;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Remove the restriction forcing cameras close near a wall")
public class CameraClip extends Feature {

    @EventListener(events = {EventClipCamera.class})
    private void runMethod(EventClipCamera eventClipCamera) {
        eventClipCamera.cancel();
    }

}
