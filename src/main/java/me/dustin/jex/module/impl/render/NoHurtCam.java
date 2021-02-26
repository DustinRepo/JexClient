package me.dustin.jex.module.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventHurtCam;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;

@ModClass(name = "NoHurtCam", category = ModCategory.VISUAL, description = "Remove the hurt-cam effect that bobs your view when damaged.")
public class NoHurtCam extends Module {

    @EventListener(events = {EventHurtCam.class})
    private void runMethod(EventHurtCam eventHurtCam) {
        eventHurtCam.cancel();
    }

}
