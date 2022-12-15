package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;

public class Hitboxes extends Feature {

    public final Property<Float> expandXProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandX")
            .value(0.1f)
            .min(0f)
            .max(1f)
            .inc(0.1f)
            .build();
    
     public final Property<Float> expandYProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandY")
            .value(0.1f)
            .min(0f)
            .max(1f)
            .inc(0.1f)
            .build();
    
     public final Property<Float> expandZProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandZ")
            .value(0.1f)
            .min(0f)
            .max(1f)
            .inc(0.1f)
            .build();
    
    	public final Property<Boolean> playerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player")
            .value(true)
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

    public Hitboxes() {
        super(Category.COMBAT, "Resize entity hitboxes to make them easier to hit");
    }

    @EventPointer
    private final EventListener<EventEntityHitbox> eventEntityHitboxEventListener = new EventListener<>(event -> {
        public boolean isEnabled(Entity entity) {
        if (event.getEntity() == null || Wrapper.INSTANCE.getLocalPlayer() == null || event.getEntity().getId() == Wrapper.INSTANCE.getLocalPlayer().getId())
            return;
			if (specificFilterProperty.value()) {
            if (entity instanceof IronGolemEntity)
                return ironGolemProperty.value();
            if (entity instanceof ZombifiedPiglinEntity)
                return zombiePiglinProperty.value();
            if (entity instanceof PiglinEntity)
                return piglinProperty.value();
        }
         if (EntityHelper.INSTANCE.isPassiveMob(entity) && !EntityHelper.INSTANCE.doesPlayerOwn(entity))
            return passiveProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return bossProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostileProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralProperty.value();
        return false;
    }
event.setBox(event.getBox().expand(expandXProperty.value(), expandYProperty.value(), expandZProperty.value()));        
    });
}
