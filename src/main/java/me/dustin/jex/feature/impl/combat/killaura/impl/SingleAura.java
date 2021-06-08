package me.dustin.jex.feature.impl.combat.killaura.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.FeatureExtension;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.combat.AutoPot;
import me.dustin.jex.feature.impl.combat.killaura.Killaura;
import me.dustin.jex.feature.impl.player.AutoEat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class SingleAura extends FeatureExtension {

    private Killaura killaura;
    private LivingEntity target;

    public SingleAura() {
        super("Single", Killaura.class);
    }

    @Override
    public void pass(Event event1) {
        if (killaura == null) {
            killaura = (Killaura) Feature.get(Killaura.class);
        }
        if (((AutoPot) Feature.get(AutoPot.class)).throwing)
            return;
        if (AutoEat.isEating || BaritoneHelper.INSTANCE.isTakingControl())
            return;
        if (event1 instanceof EventPlayerPackets) {
            EventPlayerPackets event = (EventPlayerPackets) event1;
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                if (target == null || !killaura.isValid(target, true)) {
                    target = getClosest();
                }

                if (target != null) {
                    if (killaura.rotate) {
                        RotationVector rotationVector = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), target);
                        if (killaura.randomize) {
                            rotationVector = PlayerHelper.INSTANCE.getRotations(target, killaura.randomWidth, killaura.randomHeight);
                        }
                        event.setRotation(rotationVector);
                        Wrapper.INSTANCE.getLocalPlayer().headYaw = event.getYaw();
                        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = event.getYaw();
                        if (killaura.lockview) {
                            PlayerHelper.INSTANCE.setRotation(event.getRotation());
                        }
                    }
                }
                if ((EntityHelper.INSTANCE.isAuraBlocking()) && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.unblock();
            }
            if (killaura.attackMode.equalsIgnoreCase(event.getMode().toString()))
                doAttack();
        }
        if (event1 instanceof EventRender3D) {
            if (target != null && killaura.showTarget) {
                Render3DHelper.INSTANCE.drawEntityBox(((EventRender3D) event1).getMatrixStack(), target, ((EventRender3D) event1).getPartialTicks(), killaura.targetColor);
            }
            if (killaura.reachCircle) {
                MatrixStack matrixStack = ((EventRender3D) event1).getMatrixStack();
                matrixStack.push();
                Render3DHelper.INSTANCE.setup3DRender(false);
                RenderSystem.lineWidth(1);
                double x = Wrapper.INSTANCE.getLocalPlayer().prevX + ((Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().prevX) * ((EventRender3D) event1).getPartialTicks());
                double y = Wrapper.INSTANCE.getLocalPlayer().prevY + ((Wrapper.INSTANCE.getLocalPlayer().getY() - Wrapper.INSTANCE.getLocalPlayer().prevY) * ((EventRender3D) event1).getPartialTicks());
                double z = Wrapper.INSTANCE.getLocalPlayer().prevZ + ((Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().prevZ) * ((EventRender3D) event1).getPartialTicks());
                Render3DHelper.INSTANCE.drawSphere(((EventRender3D) event1).getMatrixStack(), killaura.reach, 25, killaura.reachCircleColor, true, new Vec3d(x, y, z).subtract(0, Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()), 0));
                Render3DHelper.INSTANCE.end3DRender();
                matrixStack.pop();
            }
        }
    }

    public void doAttack() {
        boolean reblock = false;
        LivingEntity savedTarget = null;
        if (killaura.rayTrace && target != null) {
            savedTarget = target;
            Entity possible = PlayerHelper.INSTANCE.getCrosshairEntity(Wrapper.INSTANCE.getMinecraft().getTickDelta(), PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), target), killaura.reach);
            if (possible != null && possible instanceof LivingEntity) {
                target = (LivingEntity) possible;
            }
        }
        boolean alreadyBlocking = false;
        if (killaura.autoBlock && killaura.autoblockDistance > killaura.reach) {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (killaura.isValid(entity, false) && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.autoblockDistance) {
                    PlayerHelper.INSTANCE.block(killaura.ignoreNewCombat);
                    alreadyBlocking = true;
                    break;
                }
            }
        }
        if (target != null && Wrapper.INSTANCE.getWorld().getEntityById(target.getId()) != null) {
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (killaura.baritoneOverride && BaritoneHelper.INSTANCE.isBaritoneRunning())
                    BaritoneHelper.INSTANCE.followUntilDead(target, killaura);
            }
            if (killaura.autoBlock && !alreadyBlocking)
                PlayerHelper.INSTANCE.block(killaura.ignoreNewCombat);

            if (killaura.canSwing()) {
                if (EntityHelper.INSTANCE.isAuraBlocking()) {
                    reblock = true;
                    PlayerHelper.INSTANCE.unblock();
                }
                Wrapper.INSTANCE.getInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), target);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                if (killaura.autoBlock && reblock) {
                    PlayerHelper.INSTANCE.block(killaura.ignoreNewCombat);
                }
                if (savedTarget != null)
                    target = savedTarget;
            }
        } else {
            target = null;
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (!killaura.bFollowUntilDead)
                    BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
            }
        }
    }

    public LivingEntity getClosest() {
        LivingEntity livingEntity = null;
        float distance = killaura.reach;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity1 = (LivingEntity) entity;
                if (killaura.isValid(livingEntity1, true) && livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= distance) {
                    livingEntity = livingEntity1;
                    distance = livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer());
                }
            }
        }
        return livingEntity;
    }
}
