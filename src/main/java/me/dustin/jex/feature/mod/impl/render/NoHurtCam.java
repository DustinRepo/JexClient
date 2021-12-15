package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventHurtCam;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Remove the hurt-cam effect that bobs your view when damaged.")
public class NoHurtCam extends Feature {
	@EventPointer
	private final EventListener<EventHurtCam> eventHurtCamEventListener = new EventListener<>(event -> event.cancel());
}
