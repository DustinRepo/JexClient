package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderBossBar;

public class UIDisabler extends Feature {
 
  public final Property<Boolean> bossbarProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BossBar")
            .value(true)
            .build();
	
public final Property<Boolean> removehudProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("BossBar")
            .value(true)
            .build();
			
public UIDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }

public static UIDisabler INSTANCE;
private boolean bossbar = Boolean.getBoolean(bossbarProperty.value())
private boolean removehud = Boolean.getBoolean(removehudProperty.value())
}
