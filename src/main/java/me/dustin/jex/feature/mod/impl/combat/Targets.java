package me.dustin.jex.feature.mod.impl.settings;

import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.core.Category;

public class Targets extends Feature {
public static Targets INSTANCE;
    public final Property<Boolean> playerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player")
            .value(true)
            .build();
    public final Property<Boolean> friendProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Friends")
            .value(true)
            .parent(playerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> neutralProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutral")
            .value(false)
            .build();
    public final Property<Boolean> bossProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Boss")
            .value(true)
            .build();
    public final Property<Boolean> hostileProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hostile")
            .value(true)
            .build();
    public final Property<Boolean> passiveProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Passive")
            .value(true)
            .build();
    public final Property<Boolean> petProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Pet")
            .value(false)
	    .parent(passiveProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> specificFilterProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Specific Filter")
            .value(true)
            .build();
    public final Property<Boolean> ironGolemProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Iron Golem")
            .value(true)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> piglinProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Piglin")
            .value(true)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> zombiePiglinProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Zombie Piglin")
            .value(false)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> deadProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Dead")
            .value(false)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
	public final Property<Boolean> nolivingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("NoLiving")
            .value(false)
            .parent(specificFilterProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    
    public final Property<Boolean> botCheckProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Bot")
            .value(true)
            .build();
    public final Property<Boolean> teamCheckProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Team Check")
            .value(true)
	    .parent(botCheckProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> checkArmorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Check Armor")
            .value(true)
            .parent(teamCheckProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> nametaggedProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Nametagged")
            .value(true)
            .build();
    public final Property<Boolean> invisiblesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Invisibles")
            .value(true)
            .build();
    public final Property<Boolean> sleepingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Sleeping")
            .value(true)
            .build();
    public final Property<Boolean> itemProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Items")
            .value(true)
            .build();
    public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();
	
   public Targets() {
     super(Category.SETTINGS);
    }
	
	
}