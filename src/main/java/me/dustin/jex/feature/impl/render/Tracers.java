package me.dustin.jex.feature.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventBobView;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.feature.impl.render.esp.ESP;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@Feat(name = "Tracers", category = FeatureCategory.VISUAL, description = "Draw a line to entities in range.")
public class Tracers extends Feature {

    @Op(name = "Spine")
    public boolean spine;
    @Op(name = "Players")
    public boolean players = true;
    @OpChild(name = "Color on distance", parent = "Players")
    public boolean colorOnDistance;
    @Op(name = "Hostiles")
    public boolean hostiles = true;
    @Op(name = "Passives")
    public boolean passives = true;

    private boolean disableBob = false;

    @EventListener(events = {EventRender3D.EventRender3DNoBob.class})
    private void runEvent(EventRender3D.EventRender3DNoBob eventRender3D) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity && isValid((LivingEntity) entity)) {
                LivingEntity living = (LivingEntity) entity;
                Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
                assert cameraEntity != null;
                Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(living, eventRender3D.getPartialTicks());
                Color color1 = ColorHelper.INSTANCE.getColor(getColor(entity));

                Render3DHelper.INSTANCE.setup3DRender(true);
                RenderSystem.lineWidth(1.2f);

                Vec3d eyes = new Vec3d(0, 0, 1).rotateX(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).rotateY(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);//LINES doesn't fucking work for some reason so DEBUG_LINES yolo
                bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                if (spine) {
                    bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                    bufferBuilder.vertex(vec.x, vec.y + entity.getEyeHeight(entity.getPose()), vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                }
                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);

                Render3DHelper.INSTANCE.end3DRender();
            }
        });
    }

    @EventListener(events = {EventBobView.class})
    private void runMethod(EventBobView eventBobView) {
        if (disableBob)
            eventBobView.cancel();
    }

    private int getColor(Entity ent) {
        if (ent instanceof PlayerEntity && colorOnDistance) {
            PlayerEntity playerEntity = (PlayerEntity) ent;
            if (!Friend.isFriend(playerEntity.getName().asString())) {
                return getColor(ent.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) / 128).getRGB();
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
            return players && !EntityHelper.INSTANCE.isNPC((PlayerEntity) e);
        if (EntityHelper.INSTANCE.isHostileMob(e))
            return hostiles;
        if (EntityHelper.INSTANCE.isPassiveMob(e))
            return passives;
        return false;
    }
}
