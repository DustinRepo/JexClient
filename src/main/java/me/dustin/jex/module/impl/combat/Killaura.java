package me.dustin.jex.module.impl.combat;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.RotationVector;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.module.impl.player.AutoEat;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

@ModClass(name = "Aura", category = ModCategory.COMBAT, description = "Attack entities around you.")
public class Killaura extends Module {

    @Op(name = "Attack", all = {"Pre", "Post"})
    public String attackMode = "Pre";
    @Op(name = "Reach", min = 3, max = 6, inc = 0.1f)
    public float reach = 3.8f;
    @Op(name = "Ticks Exsited", max = 200, inc = 1f)
    public float ticksExisted = 50;
    @OpChild(name = "APS", min = 1, max = 20, inc = 0.1f, parent = "Ignore 1.9")
    public float aps = 10;
    @Op(name = "Ignore 1.9")
    public boolean ignoreNewCombat = false;

    @Op(name = "Baritone Override")
    public boolean baritoneOverride = true;
    @OpChild(name = "Follow until dead", parent = "Baritone Override")
    public boolean bFollowUntilDead = true;
    @OpChild(name = "Min Distance", min = 0, max = 6, inc = 0.1f, parent = "Baritone Override")
    public float bMinDist = 3;
    @Op(name = "AutoBlock")
    public boolean autoBlock = true;
    @Op(name = "Player")
    public boolean player = true;
    @Op(name = "Hostile")
    public boolean hostile = true;
    @Op(name = "Passive")
    public boolean passive = true;
    @Op(name = "RayTrace")
    public boolean rayTrace = false;
    @Op(name = "Rotate")
    public boolean rotate = true;
    @OpChild(name = "Lockview", parent = "Rotate")
    public boolean lockview = false;
    @Op(name = "Randomize")
    public boolean randomize = false;
    @OpChild(name = "Random Width", min = 0.2f, inc = 0.05f, parent = "Randomize")
    public float randomWidth = 0.5f;
    @OpChild(name = "Random Height", min = 0.2f, inc = 0.05f, parent = "Randomize")
    public float randomHeight = 0.5f;
    @Op(name = "Bot Check")
    public boolean botCheck = true;
    @Op(name = "Team Check")
    public boolean teamCheck = false;
    @OpChild(name = "Check Armor", parent = "Team Check")
    public boolean checkArmor = true;
    @Op(name = "Invisibles")
    public boolean invisibles = true;
    @Op(name = "Ignore Walls")
    public boolean ignoreWalls = true;
    @Op(name = "Show Target")
    public boolean showTarget = true;
    @OpChild(name = "Target Color", isColor = true, parent = "Show Target")
    public int targetColor = 0xff000000;
    @Op(name = "Reach Sphere")
    public boolean reachCircle = false;
    @OpChild(name = "Sphere Color", isColor = true, parent = "Reach Sphere")
    public int reachCircleColor = 0xff00ff00;

    private Timer timer = new Timer();
    private LivingEntity target;

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class}, priority = EventPriority.LOWEST)
    public void runEvent(Event event1) {
        if (((AutoPot) Module.get(AutoPot.class)).throwing)
            return;
        if (AutoEat.isEating || BaritoneHelper.INSTANCE.isTakingControl())
            return;
        if (event1 instanceof EventPlayerPackets) {
            setSuffix(this.attackMode);
            EventPlayerPackets event = (EventPlayerPackets) event1;
            if (event.getMode() == EventPlayerPackets.Mode.PRE) {
                if (target == null || !isValid(target, true)) {
                    target = getClosest();
                }

                if (target != null) {
                    if (rotate) {
                        RotationVector rotationVector = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), target);
                        if (randomize) {
                            rotationVector = PlayerHelper.INSTANCE.getRotations(target, randomWidth, randomHeight);
                        }
                        event.setRotation(rotationVector);
                        Wrapper.INSTANCE.getLocalPlayer().headYaw = event.getYaw();
                        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = event.getYaw();
                        if (lockview) {
                            Wrapper.INSTANCE.getLocalPlayer().yaw = event.getYaw();
                            Wrapper.INSTANCE.getLocalPlayer().pitch = event.getPitch();
                        }
                    }
                }
                if ((isBlocking() || EntityHelper.INSTANCE.isAuraBlocking()) && PlayerHelper.INSTANCE.isMoving())
                    unblock();
            }
            if (attackMode.equalsIgnoreCase(event.getMode().toString()))
                doAttack();
        }
        if (event1 instanceof EventRender3D) {
            if (target != null && showTarget) {
                Vec3d vec = Render3DHelper.INSTANCE.getEntityRenderPosition(target, ((EventRender3D) event1).getPartialTicks());
                Render3DHelper.INSTANCE.drawEntityBox(target, vec.x, vec.y, vec.z, targetColor);
            }
            if (reachCircle) {
                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                //GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glLineWidth(1);
                Vec3d renderPos = Render3DHelper.INSTANCE.getEntityRenderPosition(Wrapper.INSTANCE.getLocalPlayer(), ((EventRender3D) event1).getPartialTicks()).add(0, Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Wrapper.INSTANCE.getLocalPlayer().getPose()), 0);
                GL11.glTranslated(renderPos.x, renderPos.y, renderPos.z);
                Render2DHelper.INSTANCE.glColor(reachCircleColor);
                GL11.glPointSize(3f);
                GL11.glRotated(90, 1, 0, 0);
                Sphere sphere = new Sphere();
                sphere.setDrawStyle(GLU.GLU_SILHOUETTE);
                sphere.setNormals(GLU.GLU_SMOOTH);
                sphere.draw(reach, (int)30, 20);
                GL11.glTranslated(-renderPos.x, -renderPos.y, -renderPos.z);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                //GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();
            }
        }
    }

    public void doAttack() {
        boolean reblock = false;
        LivingEntity savedTarget = null;
        if (rayTrace && target != null) {
            savedTarget = target;
            Entity possible = PlayerHelper.INSTANCE.getCrosshairEntity(Wrapper.INSTANCE.getMinecraft().getTickDelta(), PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), target), reach);
            if (possible != null && possible instanceof LivingEntity) {
                target = (LivingEntity) possible;
            }
        }
        if (target != null && Wrapper.INSTANCE.getWorld().getEntityById(target.getEntityId()) != null) {
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (baritoneOverride && BaritoneHelper.INSTANCE.isBaritoneRunning())
                    BaritoneHelper.INSTANCE.followUntilDead(target, this);
            }

            if (autoBlock)
                block();

            if (canSwing()) {
                if (EntityHelper.INSTANCE.isAuraBlocking() || isBlocking()) {
                    reblock = true;
                    unblock();
                }
                Wrapper.INSTANCE.getInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), target);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                if (autoBlock && reblock) {
                    block();
                }
                if (savedTarget != null)
                    target = savedTarget;
            }
        } else {
            target = null;
            if (BaritoneHelper.INSTANCE.baritoneExists()) {
                if (!bFollowUntilDead)
                    BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
            }
        }
    }

    public LivingEntity getClosest() {
        LivingEntity livingEntity = null;
        float distance = reach;
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity1 = (LivingEntity) entity;
                if (isValid(livingEntity1, true) && livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) <= distance) {
                    livingEntity = livingEntity1;
                    distance = livingEntity1.distanceTo(Wrapper.INSTANCE.getLocalPlayer());
                }
            }
        }
        return livingEntity;
    }

    public void block() {
        if (ignoreNewCombat) {
            if (Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() instanceof SwordItem)
                Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND.MAIN_HAND);
            Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.OFF_HAND);
        } else {
            if (Wrapper.INSTANCE.getLocalPlayer().getOffHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() instanceof ShieldItem)
                Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND);
            Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.OFF_HAND);
        }
    }

    public boolean canSwing() {
        if (ignoreNewCombat) {
            if (timer.hasPassed((long) (1000 / aps))) {
                timer.reset();
                return true;
            }
        } else {
            if (Wrapper.INSTANCE.getLocalPlayer().getAttackCooldownProgress(0) == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlocking() {
        if (Wrapper.INSTANCE.getLocalPlayer().isUsingItem())
            if (Wrapper.INSTANCE.getLocalPlayer().getActiveItem() != null && Wrapper.INSTANCE.getLocalPlayer().getActiveItem().getItem() instanceof ShieldItem)
                return true;

        return false;
    }

    public void unblock() {
        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
    }

    public boolean isValid(Entity entity, boolean rangecheck) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() != null) {
            if (entity == Wrapper.INSTANCE.getLocalPlayer().getVehicle())
                return false;
        }
        if (((LivingEntity) entity).isSleeping())
            return false;
        if (!Wrapper.INSTANCE.getLocalPlayer().canSee(entity)) {
            if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > 3) {
                return false;
            }
        } else {
            if (rangecheck) {
                if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > reach) {
                    return false;
                }
            } else {
                if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > 8) {
                    return false;
                }
            }
        }
        if (entity.age < ticksExisted) {
            return false;
        }
        if (entity.isInvisible() && !invisibles) {
            return false;
        }
        if (!entity.isAlive() || (((LivingEntity) entity).getHealth() <= 0 && !Double.isNaN(((LivingEntity) entity).getHealth()))) {
            return false;
        }
        if (!Wrapper.INSTANCE.getLocalPlayer().canSee(entity) && !ignoreWalls) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity != Wrapper.INSTANCE.getLocalPlayer()) {
            if (Friend.isFriend(entity.getName().getString())) {
                return false;
            }
            if (EntityHelper.INSTANCE.isOnSameTeam((PlayerEntity) entity, Wrapper.INSTANCE.getLocalPlayer(), checkArmor) && teamCheck) {
                return false;
            }
            if (((PlayerEntity) entity).isSleeping()) {
                return false;
            }
            if (botCheck && isBot((PlayerEntity) entity)) {
                return false;
            }
            return player;
        }
        if (EntityHelper.INSTANCE.isPassiveMob((LivingEntity) entity) && !EntityHelper.INSTANCE.doesPlayerOwn((LivingEntity) entity)) {
            return passive;
        }
        if (EntityHelper.INSTANCE.isHostileMob((LivingEntity) entity)) {
            return hostile;
        }
        return false;
    }

    public boolean isBot(PlayerEntity playerEntity) {
        if (EntityHelper.INSTANCE.isNPC(playerEntity)) {
            return true;
        } else {
            //TODO: Add this shit back
            return false;//(!playerEntity.swung && !playerEntity.touchedGround) || playerEntity.getGameProfile().getProperties().isEmpty();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (BaritoneHelper.INSTANCE.baritoneExists())
            BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
    }

}
