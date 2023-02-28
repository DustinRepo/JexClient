package me.dustin.jex.feature.mod.impl.combat.killaura.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.AutoPot;
import me.dustin.jex.feature.mod.impl.player.AutoEat;

public class SingleAura extends FeatureExtension {

    private Entity target;
    private LivingEntity livingtarget;

    public SingleAura() {
        super(KillAura.TargetMode.SINGLE, KillAura.class);
    }

    @Override
    public void pass(Event event1) {
        if (Feature.get(AutoPot.class).throwing)
            return;
        if (AutoEat.isEating || BaritoneHelper.INSTANCE.isTakingControl())
            return;
        if (event1 instanceof EventPlayerPackets event) {
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                if (target == null || !KillAura.INSTANCE.isValid(target, true)) {
                    target = getClosest();
                }
                if (livingtarget == null || !KillAura.INSTANCE.isValid(livingtarget, true)) {
                    livingtarget = getLivingClosest();
                }
                KillAura.INSTANCE.setHasTarget(target != null);
                if (target != null) {
                    if (KillAura.INSTANCE.rotateProperty.value()) {
                        RotationVector rotationVector = PlayerHelper.INSTANCE.rotateToEntity(target);
                        if (KillAura.INSTANCE.randomizeProperty.value()) {
                            rotationVector = PlayerHelper.INSTANCE.randomRotateTo(target, KillAura.INSTANCE.randomWidthProperty.value(), KillAura.INSTANCE.randomHeightProperty.value());
                        }
                        event.setRotation(rotationVector);
                        Wrapper.INSTANCE.getLocalPlayer().headYaw = event.getYaw();
                        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = event.getYaw();
                        if (KillAura.INSTANCE.lockviewProperty.value()) {
                            PlayerHelper.INSTANCE.setRotation(event.getRotation());
                        }
                    }
                }
                if ((EntityHelper.INSTANCE.isAuraBlocking()) && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.unblock();
            }
            if (KillAura.INSTANCE.attackTimingProperty.value().name().equalsIgnoreCase(event.getMode().toString()))
                doAttack();
        }
        if (event1 instanceof EventRender3D) {
            if (target != null && KillAura.INSTANCE.showTargetProperty.value()) {
                Render3DHelper.INSTANCE.drawEntityBox(((EventRender3D) event1).getPoseStack(), target, ((EventRender3D) event1).getPartialTicks(), KillAura.INSTANCE.targetColorProperty.value().getRGB());
            }
            if (KillAura.INSTANCE.reachCircleProperty.value()) {
                MatrixStack matrixStack = ((EventRender3D) event1).getPoseStack();
                matrixStack.push();
                Render3DHelper.INSTANCE.setup3DRender(false);
                RenderSystem.lineWidth(1);
                double x = Wrapper.INSTANCE.getLocalPlayer().prevX + ((Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().prevX) * ((EventRender3D) event1).getPartialTicks());
                double y = Wrapper.INSTANCE.getLocalPlayer().prevY + ((Wrapper.INSTANCE.getLocalPlayer().getY() - Wrapper.INSTANCE.getLocalPlayer().prevY) * ((EventRender3D) event1).getPartialTicks());
                double z = Wrapper.INSTANCE.getLocalPlayer().prevZ + ((Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().prevZ) * ((EventRender3D) event1).getPartialTicks());
                Render3DHelper.INSTANCE.drawSphere(((EventRender3D) event1).getPoseStack(), KillAura.INSTANCE.reachProperty.value(), 25, KillAura.INSTANCE.reachCircleColorProperty.value().getRGB(), true, new Vec3d(x, y, z).subtract(0, Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()), 0));
                Render3DHelper.INSTANCE.end3DRender();
                matrixStack.pop();
            }
        }
    }

    public void doAttack() {
        boolean reblock = false;
        Entity savedTarget = null;
        if (KillAura.INSTANCE.rayTraceProperty.value() && target != null) {
            savedTarget = target;
            Entity possible = PlayerHelper.INSTANCE.getCrosshairEntity(Wrapper.INSTANCE.getMinecraft().getTickDelta(), PlayerHelper.INSTANCE.rotateToEntity(target), KillAura.INSTANCE.reachProperty.value());
            if (possible != null && possible instanceof LivingEntity) {
                target = (LivingEntity) possible;
            }
        }
        boolean alreadyBlocking = false;
        if (KillAura.INSTANCE.autoBlockProperty.value() && KillAura.INSTANCE.autoBlockDistanceProperty.value() > KillAura.INSTANCE.reachProperty.value()) {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (KillAura.INSTANCE.isValid(entity, false) && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= KillAura.INSTANCE.autoBlockDistanceProperty.value()) {
                    PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombatProperty.value());
                    alreadyBlocking = true;
                    break;
                }
            }
        }
        if (target != null && Wrapper.INSTANCE.getWorld().getEntityById(target.getId()) != null) {
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (KillAura.INSTANCE.baritoneOverrideProperty.value())
                    if (BaritoneHelper.INSTANCE.isBaritoneRunning() && !(Feature.getState(Excavator.class) && Feature.get(Excavator.class).isPaused()))
                        BaritoneHelper.INSTANCE.followUntilDead(livingtarget, KillAura.INSTANCE);
            }
            
            if (KillAura.INSTANCE.autoBlockProperty.value() && !alreadyBlocking)
                PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombatProperty.value());

            if (KillAura.INSTANCE.canSwing()) {
                if (EntityHelper.INSTANCE.isAuraBlocking()) {
                    reblock = true;
                    PlayerHelper.INSTANCE.unblock();
                }
                Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), target);
                if (KillAura.INSTANCE.swingProperty.value()) {
                PlayerHelper.INSTANCE.swing(Hand.MAIN_HAND);
                }
                if (KillAura.INSTANCE.autoBlockProperty.value() && reblock) {
                    PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombatProperty.value());
                }
                if (savedTarget != null)
                    target = savedTarget;
            }
        } else {
            target = null;
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (!KillAura.INSTANCE.followUntilDeadProperty.value())
                    BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
            }
        }
    }

    public Entity getClosest() {
        Entity livingEntity = null;
        float distance = KillAura.INSTANCE.reachProperty.value();
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof Entity entity1) {
                if (KillAura.INSTANCE.isValid(entity1, true) && entity1.distanceTo(Freecam.playerEntity != null ? Freecam.playerEntity : Wrapper.INSTANCE.getLocalPlayer()) <= distance) {
                    livingEntity = entity1;
                    distance = entity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer());
                }
            }
        }
        return livingEntity;
    }
    public LivingEntity getLivingClosest() {
        LivingEntity livingEntity0 = null;
        float distance0 = KillAura.INSTANCE.reachProperty.value();
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity livingEntity1) {
                if (KillAura.INSTANCE.isValid(livingEntity1, true) && livingEntity1.distanceTo(Freecam.playerEntity != null ? Freecam.playerEntity : Wrapper.INSTANCE.getLocalPlayer()) <= distance0) {
                    livingEntity0 = livingEntity1;
                    distance0 = livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer());
                }
            }
        }
        return livingEntity0;
    }
}
