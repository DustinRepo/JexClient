package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.render.EventRenderBossBar;
import me.dustin.events.core.annotate.EventPointer;

public class OverlayDisabler extends Feature {
 
  public final Property<Boolean> bossbarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BossBar")
            .value(true)
            .build();
			
	public OverlayDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }
@EventPointer
	private final EventListener<EventRenderBossBar> eventRenderBossBarEventListener = new EventListener<>(event -> event.cancel());	
}
