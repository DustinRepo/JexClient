package me.dustin.jex.feature.mod.impl.combat.aimbot.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.combat.aimbot.Aimbot;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.AutoPot;
import me.dustin.jex.feature.mod.impl.player.AutoEat;

public class SingleAimbot extends FeatureExtension {

    private LivingEntity target;

    public SingleAimbot() {
        super(Aimbot.TargetMode.SINGLE, Aimbot.class);
    }

    @Override
    public void pass(Event event1) {
        if (Feature.get(AutoPot.class).throwing)
            return;
        if (AutoEat.isEating || BaritoneHelper.INSTANCE.isTakingControl())
            return;
        if (event1 instanceof EventPlayerPackets event) {
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                if (target == null || !Aimbot.INSTANCE.isValid(target, true)) {
                    target = getClosest();
                }
                Aimbot.INSTANCE.setHasTarget(target != null);
                if (target != null) {
                    if (Aimbot.INSTANCE.rotateProperty.value()) {
                        RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToEntity(target);
                        if (Aimbot.INSTANCE.randomizeProperty.value()) {
                            rotationVector = PlayerHelper.INSTANCE.randomRotateTo(target, Aimbot.INSTANCE.randomWidthProperty.value(), Aimbot.INSTANCE.randomHeightProperty.value());
                        }
                        event.setRotation(rotationVector);
                        Wrapper.INSTANCE.getLocalPlayer().headYaw = event.getYaw();
                        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = event.getYaw();
                        if (Aimbot.INSTANCE.lockviewProperty.value()) {
                            PlayerHelper.INSTANCE.setRotation(event.getRotation());
                        }
                    }
                }
            }
        }
if (event1 instanceof EventRender3D) {
            if (Aimbot.INSTANCE.reachCircleProperty.value()) {
                MatrixStack matrixStack = ((EventRender3D) event1).getPoseStack();
                matrixStack.push();
                Render3DHelper.INSTANCE.setup3DRender(false);
                RenderSystem.lineWidth(1);
                double x = Wrapper.INSTANCE.getLocalPlayer().prevX + ((Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().prevX) * ((EventRender3D) event1).getPartialTicks());
                double y = Wrapper.INSTANCE.getLocalPlayer().prevY + ((Wrapper.INSTANCE.getLocalPlayer().getY() - Wrapper.INSTANCE.getLocalPlayer().prevY) * ((EventRender3D) event1).getPartialTicks());
                double z = Wrapper.INSTANCE.getLocalPlayer().prevZ + ((Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().prevZ) * ((EventRender3D) event1).getPartialTicks());
                Render3DHelper.INSTANCE.drawSphere(((EventRender3D) event1).getPoseStack(), Aimbot.INSTANCE.reachProperty.value(), 25, Aimbot.INSTANCE.reachCircleColorProperty.value().getRGB(), true, new Vec3d(x, y, z).subtract(0, Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()), 0));
                Render3DHelper.INSTANCE.end3DRender();
                matrixStack.pop();
            }
        }
    }
	
    public LivingEntity getClosest() {
        LivingEntity livingEntity = null;
        float distance = Aimbot.INSTANCE.reachProperty.value();
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity livingEntity1) {
                if (Aimbot.INSTANCE.isValid(livingEntity1, true) && livingEntity1.distanceTo(Freecam.playerEntity != null ? Freecam.playerEntity : Wrapper.INSTANCE.getLocalPlayer()) <= distance) {
                    livingEntity = livingEntity1;
                    distance = livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer());
                }
            }
        }
        return livingEntity;
    }
}
