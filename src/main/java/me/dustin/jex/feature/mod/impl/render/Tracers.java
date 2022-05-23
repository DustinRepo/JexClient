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
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class Tracers extends Feature {

    public final Property<Boolean> spineProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Spine")
            .value(false)
            .build();
    public final Property<Boolean> playersProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Players")
            .value(true)
            .build();
    public final Property<Boolean> colorOnDistanceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Color on distance")
            .value(true)
            .parent(playersProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> bossesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Bosses")
            .value(true)
            .build();
    public final Property<Boolean> hostilesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hostiles")
            .value(true)
            .build();
    public final Property<Boolean> passivesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Passives")
            .value(true)
            .build();
    public final Property<Boolean> neutralsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutrals")
            .value(true)
            .build();

    public Tracers() {
        super(Category.VISUAL, "Draw a line to entities in range.");
    }

    @EventPointer
    private final EventListener<EventRender3D.EventRender3DNoBob> eventRender3DNoBobEventListener = new EventListener<>(event -> {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity living && isValid((LivingEntity) entity)) {
                Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
                assert cameraEntity != null;
                Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(living, event.getPartialTicks());
                Color color1 = ColorHelper.INSTANCE.getColor(getColor(entity));

                Render3DHelper.INSTANCE.setup3DRender(true);
                RenderSystem.lineWidth(1.2f);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                Vec3d eyes = new Vec3d(0, 0, 1).rotateX(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).rotateY(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);//LINES doesn't fucking work for some reason so DEBUG_LINES yolo
                bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                if (spineProperty.value()) {
                    bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                    bufferBuilder.vertex(vec.x, vec.y + entity.getEyeHeight(entity.getPose()), vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                }
                BufferRenderer.drawWithShader(bufferBuilder.end());
                bufferBuilder.clear();

                Render3DHelper.INSTANCE.end3DRender();
            }
        });
    });

    private int getColor(Entity ent) {
        if (ent instanceof PlayerEntity playerEntity && colorOnDistanceProperty.value()) {
            if (!FriendHelper.INSTANCE.isFriend(playerEntity.getName().getString())) {
                return getColor(ent.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) / 64).getRGB();
            }
        }
        return ESP.INSTANCE.getColor(ent);
    }

    public Color getColor(double power) {
        if (power > 1)
            power = 1;
        double H = power * 0.35; // Hue (note 0.35 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }

    private boolean isValid(LivingEntity e) {
        if (e == null)
            return false;
        if (e == Wrapper.INSTANCE.getLocalPlayer())
            return false;
        if (e.isSleeping())
            return false;
        if (e instanceof PlayerEntity)
            return playersProperty.value() && !EntityHelper.INSTANCE.isNPC((PlayerEntity) e);
        if (EntityHelper.INSTANCE.isPassiveMob(e))
            return passivesProperty.value();
        if (EntityHelper.INSTANCE.isBossMob(e))
            return bossesProperty.value();
        if (EntityHelper.INSTANCE.isHostileMob(e))
            return hostilesProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(e))
            return neutralsProperty.value();
        return false;
    }
}
