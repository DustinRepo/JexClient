package me.dustin.jex.feature.mod.impl.combat.killaura.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.AutoPot;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class SingleAura extends FeatureExtension {

    private LivingEntity target;

    public SingleAura() {
        super("Single", KillAura.class);
    }

    @Override
    public void pass(Event event1) {
        if (((AutoPot) Feature.get(AutoPot.class)).throwing)
            return;
        if (AutoEat.isEating || BaritoneHelper.INSTANCE.isTakingControl())
            return;
        if (event1 instanceof EventPlayerPackets) {
            EventPlayerPackets event = (EventPlayerPackets) event1;
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                if (target == null || !KillAura.INSTANCE.isValid(target, true)) {
                    target = getClosest();
                }
                KillAura.INSTANCE.setHasTarget(target != null);
                if (target != null) {
                    if (KillAura.INSTANCE.rotate) {
                        RotationVector rotationVector = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), target);
                        if (KillAura.INSTANCE.randomize) {
                            rotationVector = PlayerHelper.INSTANCE.getRotations(target, KillAura.INSTANCE.randomWidth, KillAura.INSTANCE.randomHeight);
                        }
                        event.setRotation(rotationVector);
                        Wrapper.INSTANCE.getLocalPlayer().headYaw = event.getYaw();
                        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = event.getYaw();
                        if (KillAura.INSTANCE.lockview) {
                            PlayerHelper.INSTANCE.setRotation(event.getRotation());
                        }
                    }
                }
                if ((EntityHelper.INSTANCE.isAuraBlocking()) && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.unblock();
            }
            if (KillAura.INSTANCE.attackMode.equalsIgnoreCase(event.getMode().toString()))
                doAttack();
        }
        if (event1 instanceof EventRender3D) {
            if (target != null && KillAura.INSTANCE.showTarget) {
                Render3DHelper.INSTANCE.drawEntityBox(((EventRender3D) event1).getMatrixStack(), target, ((EventRender3D) event1).getPartialTicks(), KillAura.INSTANCE.targetColor);
            }
            if (KillAura.INSTANCE.reachCircle) {
                MatrixStack matrixStack = ((EventRender3D) event1).getMatrixStack();
                matrixStack.push();
                Render3DHelper.INSTANCE.setup3DRender(false);
                RenderSystem.lineWidth(1);
                double x = Wrapper.INSTANCE.getLocalPlayer().prevX + ((Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().prevX) * ((EventRender3D) event1).getPartialTicks());
                double y = Wrapper.INSTANCE.getLocalPlayer().prevY + ((Wrapper.INSTANCE.getLocalPlayer().getY() - Wrapper.INSTANCE.getLocalPlayer().prevY) * ((EventRender3D) event1).getPartialTicks());
                double z = Wrapper.INSTANCE.getLocalPlayer().prevZ + ((Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().prevZ) * ((EventRender3D) event1).getPartialTicks());
                Render3DHelper.INSTANCE.drawSphere(((EventRender3D) event1).getMatrixStack(), KillAura.INSTANCE.reach, 25, KillAura.INSTANCE.reachCircleColor, true, new Vec3d(x, y, z).subtract(0, Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()), 0));
                Render3DHelper.INSTANCE.end3DRender();
                matrixStack.pop();
            }
        }
    }

    public void doAttack() {
        boolean reblock = false;
        LivingEntity savedTarget = null;
        if (KillAura.INSTANCE.rayTrace && target != null) {
            savedTarget = target;
            Entity possible = PlayerHelper.INSTANCE.getCrosshairEntity(Wrapper.INSTANCE.getMinecraft().getTickDelta(), PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), target), KillAura.INSTANCE.reach);
            if (possible != null && possible instanceof LivingEntity) {
                target = (LivingEntity) possible;
            }
        }
        boolean alreadyBlocking = false;
        if (KillAura.INSTANCE.autoBlock && KillAura.INSTANCE.autoblockDistance > KillAura.INSTANCE.reach) {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (KillAura.INSTANCE.isValid(entity, false) && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= KillAura.INSTANCE.autoblockDistance) {
                    PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombat);
                    alreadyBlocking = true;
                    break;
                }
            }
        }
        if (target != null && Wrapper.INSTANCE.getWorld().getEntityById(target.getId()) != null) {
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (KillAura.INSTANCE.baritoneOverride && (BaritoneHelper.INSTANCE.isBaritoneRunning() || Feature.get(Excavator.class).getState()))
                    BaritoneHelper.INSTANCE.followUntilDead(target, KillAura.INSTANCE);
            }
            if (KillAura.INSTANCE.autoBlock && !alreadyBlocking)
                PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombat);

            if (KillAura.INSTANCE.canSwing()) {
                if (EntityHelper.INSTANCE.isAuraBlocking()) {
                    reblock = true;
                    PlayerHelper.INSTANCE.unblock();
                }
                Wrapper.INSTANCE.getInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), target);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                if (KillAura.INSTANCE.autoBlock && reblock) {
                    PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombat);
                }
                if (savedTarget != null)
                    target = savedTarget;
            }
        } else {
            target = null;
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (!KillAura.INSTANCE.bFollowUntilDead)
                    BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
            }
        }
    }

    public LivingEntity getClosest() {
        LivingEntity livingEntity = null;
        float distance = KillAura.INSTANCE.reach;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity1 = (LivingEntity) entity;
                if (KillAura.INSTANCE.isValid(livingEntity1, true) && livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= distance) {
                    livingEntity = livingEntity1;
                    distance = livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer());
                }
            }
        }
        return livingEntity;
    }
}
