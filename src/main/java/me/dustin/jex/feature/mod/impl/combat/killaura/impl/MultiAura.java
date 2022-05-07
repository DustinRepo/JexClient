package me.dustin.jex.feature.mod.impl.combat.killaura.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.AutoPot;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import java.util.ArrayList;

public class MultiAura extends FeatureExtension {

    private final ArrayList<LivingEntity> targets = new ArrayList<>();

    public MultiAura() {
        super("Multi", KillAura.class);
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
        if (event1 instanceof EventPlayerPackets) {
            EventPlayerPackets event = (EventPlayerPackets) event1;
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                getTargets();
                KillAura.INSTANCE.setHasTarget(!targets.isEmpty());
                if (!targets.isEmpty()) {
                    if (BaritoneHelper.INSTANCE.baritoneExists()) {
                        if (BaritoneHelper.INSTANCE.isBaritoneRunning() && !(Feature.getState(Excavator.class) && Feature.get(Excavator.class).isPaused()))
                            BaritoneHelper.INSTANCE.followUntilDead(targets.get(0), KillAura.INSTANCE);
                    }
                    if (KillAura.INSTANCE.rotate) {
                        RotationVector rotationVector = new RotationVector(PlayerHelper.INSTANCE.getYaw(), 90);
                        event.setRotation(rotationVector);
                    }
                } else {
                    if (BaritoneHelper.INSTANCE.baritoneExists())
                        if (KillAura.INSTANCE.baritoneOverride && BaritoneHelper.INSTANCE.isBaritoneRunning())
                            BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
                }
                if ((EntityHelper.INSTANCE.isAuraBlocking()) && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.unblock();
            }
            if (KillAura.INSTANCE.attackMode.equalsIgnoreCase(event.getMode().toString()))
                doAttack();
        }
        if (event1 instanceof EventRender3D) {
            getTargets();
            for (LivingEntity target : targets)
                if (target != null && KillAura.INSTANCE.showTarget) {
                    Render3DHelper.INSTANCE.drawEntityBox(((EventRender3D) event1).getPoseStack(), target, ((EventRender3D) event1).getPartialTicks(), KillAura.INSTANCE.targetColor);
                }
            if (KillAura.INSTANCE.reachCircle) {
                PoseStack matrixStack = ((EventRender3D) event1).getPoseStack();
                matrixStack.pushPose();
                Render3DHelper.INSTANCE.setup3DRender(false);
                RenderSystem.lineWidth(1);
                double x = Wrapper.INSTANCE.getLocalPlayer().xo + ((Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().xo) * ((EventRender3D) event1).getPartialTicks());
                double y = Wrapper.INSTANCE.getLocalPlayer().yo + ((Wrapper.INSTANCE.getLocalPlayer().getY() - Wrapper.INSTANCE.getLocalPlayer().yo) * ((EventRender3D) event1).getPartialTicks());
                double z = Wrapper.INSTANCE.getLocalPlayer().zo + ((Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().zo) * ((EventRender3D) event1).getPartialTicks());
                Render3DHelper.INSTANCE.drawSphere(((EventRender3D) event1).getPoseStack(), KillAura.INSTANCE.reach, 25, KillAura.INSTANCE.reachCircleColor, true, new Vec3(x, y, z).subtract(0, Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()), 0));
                Render3DHelper.INSTANCE.end3DRender();
                matrixStack.popPose();
            }
        }
    }

    public void doAttack() {
        boolean reblock = false;

        if (targets.isEmpty()) {
            if (KillAura.INSTANCE.autoBlock && KillAura.INSTANCE.autoblockDistance > KillAura.INSTANCE.reach) {
                for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
                    if (KillAura.INSTANCE.isValid(entity, false) && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= KillAura.INSTANCE.autoblockDistance) {
                        PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombat);
                        break;
                    }
                }
            }
        }else {
            if (KillAura.INSTANCE.autoBlock)
                PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombat);

            boolean canSwing = KillAura.INSTANCE.canSwing();
            if (canSwing)
                if (EntityHelper.INSTANCE.isAuraBlocking()) {
                    reblock = true;
                    PlayerHelper.INSTANCE.unblock();
                }

            for (LivingEntity target : targets) {
                if (KillAura.INSTANCE.rayTrace && target != null) {
                    Entity possible = PlayerHelper.INSTANCE.getCrosshairEntity(Wrapper.INSTANCE.getMinecraft().getFrameTime(), PlayerHelper.INSTANCE.rotateToEntity(target), KillAura.INSTANCE.reach);
                    if (possible instanceof LivingEntity && !targets.contains(possible)) {
                        target = (LivingEntity) possible;
                    }
                }

                if (target != null && Wrapper.INSTANCE.getWorld().getEntity(target.getId()) != null) {
                    if (canSwing) {
                        NetworkHelper.INSTANCE.sendPacket(ServerboundInteractPacket.createAttackPacket(target, Wrapper.INSTANCE.getLocalPlayer().isShiftKeyDown()));
                        //so crits work but also we don't have to reset our attack progress on every attack, only after
                        new EventAttackEntity(target).run();
                    }
                }
            }
            if (canSwing) {
                PlayerHelper.INSTANCE.swing(InteractionHand.MAIN_HAND);
                Wrapper.INSTANCE.getLocalPlayer().resetAttackStrengthTicker();
            }

            if (KillAura.INSTANCE.autoBlock && reblock) {
                PlayerHelper.INSTANCE.block(KillAura.INSTANCE.ignoreNewCombat);
            }
        }
    }

    public void getTargets() {
        targets.clear();
        for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity1) {
                if (KillAura.INSTANCE.isValid(livingEntity1, true)) {
                    targets.add(livingEntity1);
                }
            }
        }
    }
}
