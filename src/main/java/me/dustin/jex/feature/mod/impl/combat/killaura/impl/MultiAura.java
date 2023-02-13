package me.dustin.jex.feature.mod.impl.combat.killaura.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.AutoPot;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import java.util.ArrayList;

public class MultiAura extends FeatureExtension {

    private final ArrayList<LivingEntity> targets = new ArrayList<>();

    public MultiAura() {
        super(KillAura.TargetMode.MULTI, KillAura.class);
    }

    @Override
    public void disable() {
        targets.clear();
    }

    @Override
    public void pass(Event event1) {
        if (Feature.get(AutoPot.class).throwing)
            return;
        if (AutoEat.isEating || BaritoneHelper.INSTANCE.isTakingControl())
            return;
        if (event1 instanceof EventPlayerPackets event) {
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                getTargets();
                KillAura.INSTANCE.setHasTarget(!targets.isEmpty());
                if (!targets.isEmpty()) {
                    if (BaritoneHelper.INSTANCE.baritoneExists()) {
                        if (BaritoneHelper.INSTANCE.isBaritoneRunning() && !(Feature.getState(Excavator.class) && Feature.get(Excavator.class).isPaused()))
                            BaritoneHelper.INSTANCE.followUntilDead(targets.get(0), KillAura.INSTANCE);
                    }
                    if (KillAura.INSTANCE.rotateProperty.value()) {
                        RotationVector rotationVector = new RotationVector(PlayerHelper.INSTANCE.getYaw(), 90);
                        event.setRotation(rotationVector);
                    }
                } else {
                    if (BaritoneHelper.INSTANCE.baritoneExists())
                        if (KillAura.INSTANCE.baritoneOverrideProperty.value() && BaritoneHelper.INSTANCE.isBaritoneRunning())
                            BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
                }
                if ((EntityHelper.INSTANCE.isAuraBlocking()) && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.unblock();
            }
            if (KillAura.INSTANCE.attackTimingProperty.value().name().equalsIgnoreCase(event.getMode().toString()))
                doAttack();
        }
        if (event1 instanceof EventRender3D) {
            for (LivingEntity target : targets)
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

        if (targets.isEmpty()) {
            if (KillAura.INSTANCE.autoBlockProperty.value() && KillAura.INSTANCE.autoBlockDistanceProperty.value() > KillAura.INSTANCE.reachProperty.value()) {
                for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                    if (KillAura.INSTANCE.isValid(entity, false) && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= KillAura.INSTANCE.autoBlockDistanceProperty.value()) {
                        PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombatProperty.value());
                        break;
                    }
                }
            }
        }else {
            if (KillAura.INSTANCE.autoBlockProperty.value())
                PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombatProperty.value());

            boolean canSwing = KillAura.INSTANCE.canSwing();
            if (canSwing)
                if (EntityHelper.INSTANCE.isAuraBlocking()) {
                    reblock = true;
                    PlayerHelper.INSTANCE.unblock();
                }

            for (LivingEntity target : targets) {
                if (KillAura.INSTANCE.rayTraceProperty.value() && target != null) {
                    Entity possible = PlayerHelper.INSTANCE.getCrosshairEntity(Wrapper.INSTANCE.getMinecraft().getTickDelta(), PlayerHelper.INSTANCE.rotateToEntity(target), KillAura.INSTANCE.reachProperty.value());
                    if (possible instanceof LivingEntity && !targets.contains(possible)) {
                        target = (LivingEntity) possible;
                    }
                }

                if (target != null && Wrapper.INSTANCE.getWorld().getEntityById(target.getId()) != null) {
                    if (canSwing) {
                        NetworkHelper.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.attack(target, Wrapper.INSTANCE.getLocalPlayer().isSneaking()));
                        //so crits work but also we don't have to reset our attack progress on every attack, only after
                        new EventAttackEntity(target).run();
                    }
                }
            }
            if (canSwing) {
                if (KillAura.INSTANCE.swingProperty.value())
                PlayerHelper.INSTANCE.swing(Hand.MAIN_HAND);
                Wrapper.INSTANCE.getLocalPlayer().resetLastAttackedTicks();
            }

            if (KillAura.INSTANCE.autoBlockProperty.value() && reblock) {
                PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombatProperty.value());
            }
        }
    }

    public void getTargets() {
        targets.clear();
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity livingEntity1) {
                if (KillAura.INSTANCE.isValid(livingEntity1, true)) {
                    targets.add(livingEntity1);
                }
            }
        }
    }
}
