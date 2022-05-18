package me.dustin.jex.helper.render;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;

public enum EntityPositionHelper {
    INSTANCE;
    private final HashMap<Entity, Vec3d> headPositions = Maps.newHashMap();
    private final HashMap<Entity, Vec3d> footPositions = Maps.newHashMap();

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            Vec3d head = Render2DHelper.INSTANCE.getHeadPos(entity, event.getPartialTicks(), event.getPoseStack());
            Vec3d foot = Render2DHelper.INSTANCE.getFootPos(entity, event.getPartialTicks(), event.getPoseStack());
            headPositions.put(entity, head);
            footPositions.put(entity, foot);
        });
    });

    public Vec3d getHeadPos(Entity entity) {
        return headPositions.get(entity);
    }

    public Vec3d getFootPos(Entity entity) {
        return footPositions.get(entity);
    }
}
