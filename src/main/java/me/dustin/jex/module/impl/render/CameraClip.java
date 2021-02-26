package me.dustin.jex.module.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventClipCamera;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "CameraClip", category = ModCategory.VISUAL, description = "Remove the restriction forcing cameras close near a wall")
public class CameraClip extends Module {

    @EventListener(events = {EventClipCamera.class})
    private void runMethod(EventClipCamera eventClipCamera) {
        eventClipCamera.cancel();
    }

}
