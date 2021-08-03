package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventHurtCam;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(name = "NoHurtCam", category = Feature.Category.VISUAL, description = "Remove the hurt-cam effect that bobs your view when damaged.")
public class NoHurtCam extends Feature {

	@EventListener(events = { EventHurtCam.class })
	private void runMethod(EventHurtCam eventHurtCam) {
		eventHurtCam.cancel();
	}

}
