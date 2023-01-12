package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import me.dustin.jex.feature.mod.core.Feature;

public class NoFriendDMG extends Feature {
    
     public final Property<Boolean> neutralProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutral")
            .value(false)
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

    public NoFriendDMG() {
        super(Category.COMBAT, "Prevent yourself from attacking your friends and pets");
    }

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof PlayerEntity) {
            if (FriendHelper.INSTANCE.isFriend(event.getEntity().getName().getString()))
                event.cancel();
        } else if (EntityHelper.INSTANCE.doesPlayerOwn(event.getEntity()))
            event.cancel();
        
        if (event.getEntity instanceof MobEntity) {
        if (neutralProperty.value()) {
        if (EntityHelper.INSTANCE.isPassiveMob(event.getEntity()))
            event.cancel();
        }
          if (passiveProperty.value()) {
        if (EntityHelper.INSTANCE.isNeutralMob(event.getEntity())
            event.cancel();
          }
            if (specificFilterProperty.value()) {
            if (event.getEntity() instanceof IronGolemEntity)
                return ironGolemProperty.value();
            if (event.getEntity instanceof ZombifiedPiglinEntity)
                return zombiePiglinProperty.value();
            if (event.getEntity instanceof PiglinEntity)
                return piglinProperty.value();
        }
}
    });
}
