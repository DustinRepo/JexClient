package me.dustin.jex.feature.mod.impl.render;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderBossBar;

public class UIDisabler extends Feature {
 
  public final Property<String> bossbarProperty = new Property.PropertyBuilder<String>(this.getClass())
            .name("BossBar")
            .value("true")
	    .max(5)
            .build();
	
public final Property<String> removehudProperty = new Property.PropertyBuilder<String>(this.getClass())
            .name("RemoveHud")
            .value("true")
            .max(5) 	
            .build();
			
public UIDisabler() {
        super(Category.VISUAL, "Removes unnecessary interface elements");
    }

public static UIDisabler INSTANCE;
public boolean bossbar = Boolean.valueOf(bossbarProperty);
public boolean removehud = Boolean.valueOf(removehudProperty);
}
