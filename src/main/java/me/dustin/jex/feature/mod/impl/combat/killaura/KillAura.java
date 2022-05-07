package me.dustin.jex.feature.mod.impl.combat.killaura;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.impl.MultiAura;
import me.dustin.jex.feature.mod.impl.combat.killaura.impl.SingleAura;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.baritone.BaritoneHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Attack entities around you.", key = GLFW.GLFW_KEY_R)
public class KillAura extends Feature {
    public static KillAura INSTANCE;

    @Op(name = "Mode", all = {"Single", "Multi"})
    public String mode = "Single";
    @Op(name = "Attack", all = {"Pre", "Post"})
    public String attackMode = "Pre";
    @Op(name = "Reach", min = 3, max = 6, inc = 0.1f)
    public float reach = 3.8f;
    /*@Op(name = "FOV", min = 15, max = 360)
    public int fov = 360;*/
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
    @OpChild(name = "Distance", min = 3, max = 15, inc = 0.1f, parent = "AutoBlock")
    public float autoblockDistance = 7.5f;
    @Op(name = "Player")
    public boolean player = true;
    @Op(name = "Neutral")
    public boolean neutral = false;
    @Op(name = "Hostile")
    public boolean hostile = true;
    @Op(name = "Passive")
    public boolean passive = true;
    @Op(name = "Specific Filter")
    public boolean specificFilter = false;
    @OpChild(name = "Iron Golem", parent = "Specific Filter")
    public boolean ironGolem = true;
    @OpChild(name = "Piglin", parent = "Specific Filter")
    public boolean piglin = true;
    @OpChild(name = "Zombie Piglin", parent = "Specific Filter")
    public boolean zombiepiglin = true;
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
    @Op(name = "Nametagged")
    public boolean nametagged = true;
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

    private final StopWatch stopWatch = new StopWatch();
    private String lastMode;

    private boolean hasTarget = false;

    public ArrayList<Player> touchedGround = new ArrayList<>();
    public ArrayList<Player> swung = new ArrayList<>();

    public KillAura() {
        INSTANCE = this;
        new SingleAura();
        new MultiAura();
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        for(Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
            if(entity instanceof Player playerEntity) {
                if(playerEntity.isOnGround() && !touchedGround.contains(playerEntity))
                    touchedGround.add(playerEntity);
                if(playerEntity.attackAnim > 0 && !swung.contains(playerEntity))
                    swung.add(playerEntity);
            }
        }
        for(int i = 0; i < swung.size() - 1; i++) {
            Player playerEntity = swung.get(i);
            if(playerEntity == null) {
                swung.remove(i);
            }
        }
        for(int i = 0; i < touchedGround.size() - 1; i++) {
            Player playerEntity = touchedGround.get(i);
            if(playerEntity == null) {
                touchedGround.remove(i);
            }
        }
        setSuffix(mode + " : " + attackMode);
        sendEvent(event);
    }, Priority.LAST);

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> sendEvent(event));

    public void sendEvent(Event event) {
        if (!mode.equalsIgnoreCase(lastMode) && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        lastMode = mode;
    }

    public boolean canSwing() {
        if (ignoreNewCombat) {
            if (stopWatch.hasPassed((long) (1000 / aps))) {
                stopWatch.reset();
                return true;
            }
        } else {
            if (Wrapper.INSTANCE.getLocalPlayer().getAttackStrengthScale(0) == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isValid(Entity entity, boolean rangecheck) {
        if (!(entity instanceof LivingEntity livingEntity))
            return false;
        if (entity == Wrapper.INSTANCE.getLocalPlayer() || entity == Freecam.playerEntity)
            return false;
        if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() != null) {
            if (entity == Wrapper.INSTANCE.getLocalPlayer().getVehicle())
                return false;
        }
        if (livingEntity.isSleeping())
            return false;
        if (rangecheck) {
            if (entity.distanceTo(Wrapper.INSTANCE.getPlayer()) > reach)
                return false;
            if (!(livingEntity.hasLineOfSight(Wrapper.INSTANCE.getPlayer()))) {
                if (entity.distanceTo(Wrapper.INSTANCE.getPlayer()) > 3)
                    return false;
            }
        }
        if (entity.tickCount < ticksExisted)
            return false;
        if (entity.hasCustomName() && !nametagged)
            return false;
        if (entity.isInvisible() && !invisibles)
            return false;
        if (!entity.isAlive() || (((LivingEntity) entity).getHealth() <= 0 && !Double.isNaN(((LivingEntity) entity).getHealth())))
            return false;
        if (!Wrapper.INSTANCE.getLocalPlayer().hasLineOfSight(entity) && !ignoreWalls)
            return false;
        //TODO: fix this with 180/-180 having some issues
        /*if (PlayerHelper.INSTANCE.getDistanceFromMouse(entity) * 2 > KillAura.INSTANCE.fov) {
            return false;
        }*/
        if (entity instanceof Player && entity != Wrapper.INSTANCE.getLocalPlayer()) {
            if (FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
                return false;
            if (EntityHelper.INSTANCE.isOnSameTeam((Player) entity, Wrapper.INSTANCE.getLocalPlayer(), checkArmor) && teamCheck)
                return false;
            if (botCheck && isBot((Player) entity))
                return false;
            return player;
        }
        if (specificFilter) {
            if (entity instanceof IronGolem)
                return ironGolem;
            if (entity instanceof ZombifiedPiglin)
                return zombiepiglin;
            if (entity instanceof Piglin)
                return piglin;
        }
        if (EntityHelper.INSTANCE.isPassiveMob(entity) && !EntityHelper.INSTANCE.doesPlayerOwn(entity))
            return passive;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostile;
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutral;
        return false;
    }

    public boolean isBot(Player playerEntity) {
        if (EntityHelper.INSTANCE.isNPC(playerEntity)) {
            return true;
        } else {
            return (!swung.contains(playerEntity) && !touchedGround.contains(playerEntity)) || playerEntity.getGameProfile().getProperties().isEmpty();
        }
    }

    public void setHasTarget(boolean hasTarget) {
        this.hasTarget = hasTarget;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        setHasTarget(false);
        if (BaritoneHelper.INSTANCE.baritoneExists())
            BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
        FeatureExtension.get(mode, this).disable();
    }

}
