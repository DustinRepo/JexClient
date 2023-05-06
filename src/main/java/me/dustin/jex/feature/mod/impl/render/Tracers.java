package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.BufferHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.feature.mod.impl.settings.Targets;
import me.dustin.jex.helper.render.shader.ShaderHelper;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class Tracers extends Feature {

    public final Property<Boolean> spineProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Spine")
            .value(false)
            .build();
    public final Property<Boolean> colorOnDistanceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Color on distance")
            .value(true)
            .build();

    public Tracers() {
        super(Category.VISUAL);
    }

    @EventPointer
    private final EventListener<EventRender3D.EventRender3DNoBob> eventRender3DNoBobEventListener = new EventListener<>(event -> {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (isValid(entity)) {
                Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
                if (cameraEntity == null)
                    return;
                Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, event.getPartialTicks());
                Color color1 = ColorHelper.INSTANCE.getColor(getColor(entity));

                Render3DHelper.INSTANCE.setup3DRender(true);
                RenderSystem.lineWidth(1.2f);
                Vec3d eyes = new Vec3d(0, 0, 1).rotateX(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).rotateY(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

                BufferBuilder bufferBuilder = BufferHelper.INSTANCE.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);//LINES doesn't fucking work for some reason so DEBUG_LINES yolo
                bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                if (spineProperty.value()) {
                    bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                    bufferBuilder.vertex(vec.x, vec.y + entity.getEyeHeight(entity.getPose()), vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                }
                BufferHelper.INSTANCE.drawWithShader(bufferBuilder, ShaderHelper.INSTANCE.getPosColorShader());

                Render3DHelper.INSTANCE.end3DRender();
            }
        });
    });

    private int getColor(Entity ent) {
        if (ent instanceof PlayerEntity playerEntity && colorOnDistanceProperty.value()) {
            if (!FriendHelper.INSTANCE.isFriend(playerEntity.getName().getString())) {
                return ColorHelper.INSTANCE.redGreenShift(ent.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) / 64);
            }
        }
        return ESP.INSTANCE.getColor(ent);
    }

    private boolean isValid(Entity e) {
        if (e == null)
            return false;
        if (e == Wrapper.INSTANCE.getLocalPlayer())
            return false;
        if (e instanceof PlayerEntity)
            return Targets.INSTANCE.playerProperty.value();
        if (!(EntityHelper.INSTANCE.isNPC((PlayerEntity) e)))
            return Targets.INSTANCE.botCheckProperty.value();
        if (EntityHelper.INSTANCE.isPassiveMob(e))
            return Targets.INSTANCE.passiveProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(e))
            return Targets.INSTANCE.bossProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(e))
            return Targets.INSTANCE.hostileProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(e))
            return Targets.INSTANCE.neutralProperty.value();
        if (e instanceof ItemEntity)
            return Targets.INSTANCE.itemProperty.value();
        if (!(e instanceof LivingEntity))
            return Targets.INSTANCE.nolivingProperty.value();
        return false;
    }
}
