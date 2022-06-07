package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventHurtCam;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;

public class NoHurtCam extends Feature {

	public NoHurtCam() {
		super(Category.VISUAL, "Remove the hurt-cam effect that bobs your view when damaged.");
	}

	@EventPointer
	private final EventListener<EventHurtCam> eventHurtCamEventListener = new EventListener<>(event -> event.cancel());
}
