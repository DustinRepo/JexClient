package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

public class FastPlace extends Feature {
	
	public Property<Float> delayProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("RightClickDelay (Tick)")
            .description("The amount of ticks to wait between placing blocks. Default MC is 4.")
            .value(1)
            .min(0f)
            .max(1f)
            .inc(0.05f)
            .build();

	public FastPlace() {
		super(Category.PLAYER, "Place fast.");
	}

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		Wrapper.INSTANCE.getIMinecraft().setRightClickDelayTimer(delayProperty.value());
	}, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

	@Override
	public void onDisable() {
		Wrapper.INSTANCE.getIMinecraft().setRightClickDelayTimer(4);
		super.onDisable();
	}

}
