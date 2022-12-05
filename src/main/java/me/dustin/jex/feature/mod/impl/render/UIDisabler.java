package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.EventPointer;

public class UIDisabler extends Feature {
 
  public final Property<Boolean> bossbarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BossBar")
            .value(true)
            .build();
			
			public UIDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }
@EventPointer
	private final EventListener<EventRenderBossBar> eventRenderCrosshairEventListener = new EventListener<>(event -> {
	if (bossBarProperty.value());
	event.cancel();
	});
}