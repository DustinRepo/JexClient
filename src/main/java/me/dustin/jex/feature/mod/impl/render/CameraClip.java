package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventClipCamera;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Remove the restriction forcing cameras close near a wall")
public class CameraClip extends Feature {
    @EventPointer
    private final EventListener<EventClipCamera> eventClipCameraEventListener = new EventListener<>(event -> event.cancel());
}
