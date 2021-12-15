package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Place fast.")
public class FastPlace extends Feature {

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		Wrapper.INSTANCE.getIMinecraft().setRightClickDelayTimer(0);
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

	@Override
	public void onDisable() {
		Wrapper.INSTANCE.getIMinecraft().setRightClickDelayTimer(4);
		super.onDisable();
	}

}
