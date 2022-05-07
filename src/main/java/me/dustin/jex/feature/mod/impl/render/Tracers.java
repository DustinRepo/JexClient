package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Draw a line to entities in range.")
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
    @Op(name = "Neutrals")
    public boolean neutrals = true;


    @EventPointer
    private final EventListener<EventRender3D.EventRender3DNoBob> eventRender3DNoBobEventListener = new EventListener<>(event -> {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (entity instanceof LivingEntity living && isValid((LivingEntity) entity)) {
                Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
                assert cameraEntity != null;
                Vec3 vec = Render3DHelper.INSTANCE.getEntityRenderPosition(living, event.getPartialTicks());
                Color color1 = ColorHelper.INSTANCE.getColor(getColor(entity));

                Render3DHelper.INSTANCE.setup3DRender(true);
                RenderSystem.lineWidth(1.2f);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                Vec3 eyes = new Vec3(0, 0, 1).xRot(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).yRot(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);//LINES doesn't fucking work for some reason so DEBUG_LINES yolo
                bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
                bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
                if (spine) {
                    bufferBuilder.vertex(vec.x, vec.y, vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
                    bufferBuilder.vertex(vec.x, vec.y + entity.getEyeHeight(entity.getPose()), vec.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
                }
                bufferBuilder.clear();
                BufferUploader.drawWithShader(bufferBuilder.end());

                Render3DHelper.INSTANCE.end3DRender();
            }
        });
    });

    private int getColor(Entity ent) {
        if (colorOnDistance && ent instanceof Player playerEntity) {
            if (!FriendHelper.INSTANCE.isFriend(playerEntity)) {
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
        if (e instanceof Player)
            return players && !EntityHelper.INSTANCE.isNPC((Player) e);
        if (EntityHelper.INSTANCE.isPassiveMob(e))
            return passives;
        if (EntityHelper.INSTANCE.isHostileMob(e))
            return hostiles;
        if (EntityHelper.INSTANCE.isNeutralMob(e))
            return neutrals;
        return false;
    }
}
