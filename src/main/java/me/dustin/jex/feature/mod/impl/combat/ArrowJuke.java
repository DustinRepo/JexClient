
package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

public class ArrowJuke extends Feature {

public final Property<Float> rangeProperty = new Property.PropertyBuilder<Float>(this.getClass())
        .name("Range")
        .value(5f)
        .min(1f)
        .max(6f)
        .inc(0.1f)
        .build();
	
    public ArrowJuke() {
        super(Category.COMBAT, "dodges arrows-(beta feature)");
    }
	
private final List<Vec3d> possibleMoveDirections = Arrays.asList(
        new Vec3d(1, 0, 1),
        new Vec3d(0, 0, 1),
        new Vec3d(-1, 0, 1),
        new Vec3d(1, 0, 0),
        new Vec3d(-1, 0, 0),
        new Vec3d(1, 0, -1),
        new Vec3d(0, 0, -1),
        new Vec3d(-1, 0, -1)
    );
	
private void move(Vec3d vel) {
   move(vel.x, vel.y, vel.z);
    }
    @EventPointer
    public final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
	 boolean didMove = false;
	    if (entity instanceof ArrowEntity arrowEntity) {
              if (arrowEntity.age < 75) {
            if (arrowEntity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= rangeProperty.value()) { 
            boolean didMove = false;
            Collections.shuffle(possibleMoveDirections); //Make the direction unpredictable
            for (Vec3d direction : possibleMoveDirections) {
                Vec3d velocity = direction.multiply(speed);
                if (isValid(velocity, true)) {
                    move(velocity);
                    didMove = true;
                    break;
                }
            }
            if (didMove) break;
            speed += moveSpeed.get();
       }
      }
    }
}));
}
