package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.EventListener;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderBossBar;

public class UIDisabler extends Feature {
 
  public final Property<Boolean> bossbarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BossBar")
            .value(true)
	    .max(5)
            .build();
			
public UIDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }

	@EventPointer
	private final EventListener<EventRenderBossBar> eventRenderBossBarEventListener = new EventListener<>(event -> {
          if (event.getBossBarHud() instanceof ClientBossBar && bossbarProperty.value()) {
	      event.cancel();
    }
  });
}
