package me.dustin.jex.module.impl.render;

import com.google.common.collect.Maps;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@ModClass(name = "MobOwners", category = ModCategory.VISUAL, description = "Show the names of the owners of tamed mobs")
public class OwnerTags extends Module {

    @Op(name = "Draw Faces")
    public boolean drawFaces = true;
    private HashMap<LivingEntity, Vec3d> positions = Maps.newHashMap();

    @EventListener(events = {EventRender3D.class, EventRender2D.class})
    private void runMethod(Event event) {
        if (event instanceof EventRender3D) {
            positions.clear();
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (entity instanceof LivingEntity) {
                    LivingEntity tameableEntity = (LivingEntity) entity;
                    if (EntityHelper.INSTANCE.getOwnerUUID(tameableEntity) != null) {
                        Render3DHelper.INSTANCE.applyCameraRots();
                        positions.put(tameableEntity, Render2DHelper.INSTANCE.getHeadPos(entity, ((EventRender3D) event).getPartialTicks()));
                        Render3DHelper.INSTANCE.fixCameraRots();
                    }
                }
            }
        } else if (event instanceof EventRender2D) {
            EventRender2D eventRender2D = (EventRender2D) event;
            Nametag nametagModule = (Nametag) Module.get(Nametag.class);
            positions.keySet().forEach(livingEntity -> {
                Vec3d pos = positions.get(livingEntity);
                if (isOnScreen(pos)) {
                    float x = (float) pos.x;
                    float y = (float) pos.y;
                    if (nametagModule.getState() && nametagModule.passives) {
                        y -= 12;
                    }
                    UUID uuid = EntityHelper.INSTANCE.getOwnerUUID(livingEntity);
                    String nameString = PlayerHelper.INSTANCE.getName(uuid);
                    if (nameString == null)
                        nameString = Objects.requireNonNull(uuid).toString();
                    nameString = "\247o" + nameString.trim();
                    float length = FontHelper.INSTANCE.getStringWidth(nameString);
                    Render2DHelper.INSTANCE.fill(eventRender2D.getMatrixStack(), x - (length / 2) - 2, y - 12, x + (length / 2) + 2, y - 1, 0x35000000);
                    FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), nameString, x, y - 10, -1);

                    if (drawFaces) {
                        Render2DHelper.INSTANCE.drawFace(((EventRender2D) event).getMatrixStack(), x - 8, y - 30, 2, MCAPIHelper.INSTANCE.getPlayerSkin(uuid));
                    }
                }
            });
        }
    }

    public boolean isOnScreen(Vec3d pos) {
        return pos != null && (pos.z > -1 && pos.z < 1);
    }
}
