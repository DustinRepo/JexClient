package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventEntityHitbox;
import me.dustin.jex.helper.entity.EntityHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.util.math.Box;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.settings.Targets;

public class Hitboxes extends Feature {

    public final Property<Float> expandXProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandX")
            .value(0.1f)
            .min(0f)
            .max(2f)
            .inc(0.02f)
            .build();
	
     public final Property<Float> expandYProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandY")
            .value(0.1f)
            .min(0f)
            .max(2f)
            .inc(0.02f)
            .build();
	
     public final Property<Float> expandZProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("ExpandZ")
            .value(0.1f)
            .min(0f)
            .max(2f)
            .inc(0.02f)
            .build();
	
    public Hitboxes() {
        super(Category.COMBAT);
    }

    @EventPointer
    private final EventListener<EventEntityHitbox> eventEntityHitboxEventListener = new EventListener<>(event -> {
	    Entity entity = event.getEntity();
	    if (isEnabled(entity)) {
        if (event.getEntity() == null || Wrapper.INSTANCE.getLocalPlayer() == null || event.getEntity().getId() == Wrapper.INSTANCE.getLocalPlayer().getId())
            return;
            event.setBox(event.getBox().expand(expandXProperty.value(), expandYProperty.value(), expandZProperty.value()));
	    }
});	    

public boolean isBot(PlayerEntity playerEntity) {
        if (EntityHelper.INSTANCE.isNPC(playerEntity)) {
            return true;
        } else {
            return false;
        }
    }  
      private boolean isEnabled(Entity entity) {	  
	if (Targets.INSTANCE.specificFilterProperty.value()) {
            if (entity instanceof IronGolemEntity)
                return Targets.INSTANCE.ironGolemProperty.value();
            if (entity instanceof ZombifiedPiglinEntity)
                return Targets.INSTANCE.zombiePiglinProperty.value();
            if (entity instanceof PiglinEntity)
                return Targets.INSTANCE.piglinProperty.value();
        }
	if (!(entity instanceof LivingEntity))
            return Targets.INSTANCE.nolivingProperty.value();	  
         if (EntityHelper.INSTANCE.isPassiveMob(entity))
	    return Targets.INSTANCE.passiveProperty.value();
	  if (EntityHelper.INSTANCE.doesPlayerOwn(entity))
            return Targets.INSTANCE.petProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(entity))
            return Targets.INSTANCE.bossProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return Targets.INSTANCE.hostileProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return Targets.INSTANCE.neutralProperty.value();
        if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer())
	    return Targets.INSTANCE.playerProperty.value();	
            if (FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
                return Targets.INSTANCE.friendProperty.value();
            if (EntityHelper.INSTANCE.isOnSameTeam((PlayerEntity) entity, Wrapper.INSTANCE.getLocalPlayer(), Targets.INSTANCE.teamCheckProperty.value()))
                return false;
            if (isBot((PlayerEntity) entity))
                return Targets.INSTANCE.botCheckProperty.value();   
        return false;
   }       
}
