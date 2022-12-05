package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.gui.hud.*;
import me.dustin.events.core.EventListener;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.hud.ClientBossBar;

public class OverlayDisabler extends Feature {
 
  public final Property<Boolean> bossbarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BossBar")
            .value(true)
            .build();
			
     public OverlayDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }
	
	 private final EventListener<EventRenderHud> eventRenderHudEventListener = new EventListener<>(event -> {
	 ClientBossBar event;
	 if (bossbarProperty.value()) {
                event.cancel();
            }  
	   });
}
