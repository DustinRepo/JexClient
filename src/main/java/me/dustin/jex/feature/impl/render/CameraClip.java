package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventClipCamera;
import me.dustin.jex.feature.core.Feature;

@Feature.Manifest(name = "CameraClip", category = Feature.Category.VISUAL, description = "Remove the restriction forcing cameras close near a wall")
public class CameraClip extends Feature {

    @EventListener(events = {EventClipCamera.class})
    private void runMethod(EventClipCamera eventClipCamera) {
        eventClipCamera.cancel();
    }

}
