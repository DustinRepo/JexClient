package me.dustin.jex.module.impl.combat.killaura.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.combat.AutoPot;
import me.dustin.jex.module.impl.combat.killaura.Killaura;
import me.dustin.jex.module.impl.player.AutoEat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class MultiAura extends ModuleExtension {

    private Killaura killaura;
    private ArrayList<LivingEntity> targets = new ArrayList<>();

    public MultiAura() {
        super("Multi", Killaura.class);
    }

    @Override
    public void disable() {
        targets.clear();
    }

    @Override
    public void pass(Event event1) {
        if (killaura == null) {
            killaura = (Killaura) Module.get(Killaura.class);
        }
        if (((AutoPot) Module.get(AutoPot.class)).throwing)
            return;
        if (AutoEat.isEating || BaritoneHelper.INSTANCE.isTakingControl())
            return;
        if (event1 instanceof EventPlayerPackets) {
            EventPlayerPackets event = (EventPlayerPackets) event1;
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                getTargets();

                if (!targets.isEmpty()) {
                    if (BaritoneHelper.INSTANCE.baritoneExists()) {
                        if (killaura.baritoneOverride && BaritoneHelper.INSTANCE.isBaritoneRunning())
                            BaritoneHelper.INSTANCE.followUntilDead(targets.get(0), killaura);
                    }
                    if (killaura.rotate) {
                        RotationVector rotationVector = new RotationVector(Wrapper.INSTANCE.getLocalPlayer().yaw, 90);
                        event.setRotation(rotationVector);
                    }
                } else {
                    if (BaritoneHelper.INSTANCE.baritoneExists())
                        if (killaura.baritoneOverride && BaritoneHelper.INSTANCE.isBaritoneRunning())
                            BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
                }
                if ((EntityHelper.INSTANCE.isAuraBlocking()) && PlayerHelper.INSTANCE.isMoving())
                    PlayerHelper.INSTANCE.unblock();
            }
            if (killaura.attackMode.equalsIgnoreCase(event.getMode().toString()))
                doAttack();
        }
        if (event1 instanceof EventRender3D) {
            getTargets();
            for (LivingEntity target : targets)
                if (target != null && killaura.showTarget) {
                    Render3DHelper.INSTANCE.drawEntityBox(target, ((EventRender3D) event1).getPartialTicks(), killaura.targetColor);
                }
            if (killaura.reachCircle) {
                MatrixStack matrixStack = ((EventRender3D) event1).getMatrixStack();
                matrixStack.push();
                Render3DHelper.INSTANCE.setup3DRender(false);
                RenderSystem.lineWidth(1);
                double x = Wrapper.INSTANCE.getLocalPlayer().prevX + ((Wrapper.INSTANCE.getLocalPlayer().getX() - Wrapper.INSTANCE.getLocalPlayer().prevX) * ((EventRender3D) event1).getPartialTicks());
                double y = Wrapper.INSTANCE.getLocalPlayer().prevY + ((Wrapper.INSTANCE.getLocalPlayer().getY() - Wrapper.INSTANCE.getLocalPlayer().prevY) * ((EventRender3D) event1).getPartialTicks());
                double z = Wrapper.INSTANCE.getLocalPlayer().prevZ + ((Wrapper.INSTANCE.getLocalPlayer().getZ() - Wrapper.INSTANCE.getLocalPlayer().prevZ) * ((EventRender3D) event1).getPartialTicks());
                Render3DHelper.INSTANCE.drawSphere(killaura.reach, 25, killaura.reachCircleColor, true, new Vec3d(x, y, z).subtract(0, Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()), 0));
                Render3DHelper.INSTANCE.end3DRender();
                matrixStack.pop();
            }
        }
    }

    public void doAttack() {
        boolean reblock = false;

        if (targets.isEmpty()) {
            if (killaura.autoBlock && killaura.autoblockDistance > killaura.reach) {
                for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                    if (killaura.isValid(entity, false) && Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.autoblockDistance) {
                        PlayerHelper.INSTANCE.block(killaura.ignoreNewCombat);
                        break;
                    }
                }
            }
        }else {
            if (killaura.autoBlock)
                PlayerHelper.INSTANCE.block(killaura.ignoreNewCombat);

            boolean canSwing = killaura.canSwing();
            if (canSwing)
                if (EntityHelper.INSTANCE.isAuraBlocking()) {
                    reblock = true;
                    PlayerHelper.INSTANCE.unblock();
                }

            for (LivingEntity target : targets) {
                if (killaura.rayTrace && target != null) {
                    Entity possible = PlayerHelper.INSTANCE.getCrosshairEntity(Wrapper.INSTANCE.getMinecraft().getTickDelta(), PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), target), killaura.reach);
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
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                Wrapper.INSTANCE.getLocalPlayer().resetLastAttackedTicks();
            }

            if (killaura.autoBlock && reblock) {
                PlayerHelper.INSTANCE.block(killaura.ignoreNewCombat);
            }
        }
    }

    public void getTargets() {
        targets.clear();
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity1 = (LivingEntity) entity;
                if (killaura.isValid(livingEntity1, true)) {
                    targets.add(livingEntity1);
                }
            }
        }
    }
}
