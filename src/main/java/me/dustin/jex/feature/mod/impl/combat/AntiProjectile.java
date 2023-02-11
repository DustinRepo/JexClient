package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.annotate.EventPointer;

public class AntiProjectile extends Feature {

    public final Property<Float> rangeProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Range")
            .value(5f)
            .min(1)
            .max(7)
            .inc(0.1f)
            .build();
    public final Property<Boolean> rotateProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Rotate")
            .value(true)
            .build();
	public final Property<Boolean> fireballProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("Fireball")
	    .description("Whether or not to swing fireball")
	    .value(true)
	    .build();
	public final Property<Boolean> bulletProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("ShulkerBullet")
	    .description("Whether or not to swing shulker bullet")
	    .value(true)
	    .build();
	public final Property<Boolean> skullProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
	    .name("WitherSkull")
	    .description("Whether or not to swing shulker bullet")
	    .value(true)
	    .build();

    public AntiProjectile() {
        super(Category.COMBAT, "Knock away projectile from others");
    }

    @EventPointer
	 private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
        if (entity instanceof ShulkerBulletEntity bulletEntity || entity instanceof FireballEntity fireballEntity || entity instanceof WitherSkullEntity skullEntity) {
		if (bulletProperty.value()){
		if (bulletEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= rangeProperty.value()) {
        if (rotateProperty.value()) {
        RotationVector rotation = PlayerHelper.INSTANCE.rotateToEntity(bulletEntity);
        event.setRotation(rotation);
		PlayerHelper.INSTANCE.setRotation(event.getRotation());
        }
		 Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), bulletEntity);
        }
		}
		if (fireballProperty.value()){
		if (fireballEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= rangeProperty.value()) {
        if (rotateProperty.value()) {
        RotationVector rotation = PlayerHelper.INSTANCE.rotateToEntity(fireballEntity);
        event.setRotation(rotation);
		PlayerHelper.INSTANCE.setRotation(event.getRotation());
        }
		Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), fireballEntity);
        }
		}
		if (skullProperty.value()){
		if (skullEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= rangeProperty.value()) {
        if (rotateProperty.value()) {
        RotationVector rotation = PlayerHelper.INSTANCE.rotateToEntity(skullEntity);
        event.setRotation(rotation);
		PlayerHelper.INSTANCE.setRotation(event.getRotation());
         }
		Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), skullEntity);
            }
			}
		}	
    }), new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
