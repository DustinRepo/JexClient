package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventClipCamera;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;

public class CameraClip extends Feature {

    public CameraClip() {
        super(Category.VISUAL);
    }

    @EventPointer
    private final EventListener<EventClipCamera> eventClipCameraEventListener = new EventListener<>(event -> event.cancel());
}
