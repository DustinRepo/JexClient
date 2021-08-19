package me.dustin.jex.helper.render;

import com.google.common.collect.Maps;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

public enum EntityPositionHelper {
    INSTANCE;
    private HashMap<Entity, Vec3d> headPositions = Maps.newHashMap();
    private HashMap<Entity, Vec3d> footPositions = Maps.newHashMap();

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            Vec3d head = Render2DHelper.INSTANCE.getHeadPos(entity, eventRender3D.getPartialTicks(), eventRender3D.getMatrixStack());
            Vec3d foot = Render2DHelper.INSTANCE.getFootPos(entity, eventRender3D.getPartialTicks(), eventRender3D.getMatrixStack());
            headPositions.put(entity, head);
            headPositions.put(entity, foot);
        });
    }

    public Vec3d getHeadPos(Entity entity) {
        return headPositions.get(entity);
    }

    public Vec3d getFootPos(Entity entity) {
        return footPositions.get(entity);
    }
}
