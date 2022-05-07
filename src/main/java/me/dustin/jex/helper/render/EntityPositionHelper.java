package me.dustin.jex.helper.render;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;

public enum EntityPositionHelper {
    INSTANCE;
    private final HashMap<Entity, Vec3> headPositions = Maps.newHashMap();
    private final HashMap<Entity, Vec3> footPositions = Maps.newHashMap();

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            Vec3 head = Render2DHelper.INSTANCE.getHeadPos(entity, event.getPartialTicks(), event.getPoseStack());
            Vec3 foot = Render2DHelper.INSTANCE.getFootPos(entity, event.getPartialTicks(), event.getPoseStack());
            headPositions.put(entity, head);
            footPositions.put(entity, foot);
        });
    });

    public Vec3 getHeadPos(Entity entity) {
        return headPositions.get(entity);
    }

    public Vec3 getFootPos(Entity entity) {
        return footPositions.get(entity);
    }
}
