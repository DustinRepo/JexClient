package me.dustin.jex.feature.impl.combat.killaura;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.events.core.enums.EventPriority;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.FeatureExtension;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.combat.killaura.impl.MultiAura;
import me.dustin.jex.feature.impl.combat.killaura.impl.SingleAura;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.BaritoneHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Feature.Manifest(name = "Aura", category = Feature.Category.COMBAT, description = "Attack entities around you.", key = GLFW.GLFW_KEY_R)
public class Killaura extends Feature {

    @Op(name = "Mode", all = {"Single", "Multi"})
    public String mode = "Single";
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
    @OpChild(name = "Distance", min = 3, max = 15, inc = 0.1f, parent = "AutoBlock")
    public float autoblockDistance = 7.5f;
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
    private String lastMode;

    public ArrayList<PlayerEntity> touchedGround = new ArrayList<>();
    public ArrayList<PlayerEntity> swung = new ArrayList<>();

    public Killaura() {
        new SingleAura();
        new MultiAura();
    }

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class}, priority = EventPriority.LOWEST)
    public void runEvent(Event event) {
        if (event instanceof EventPlayerPackets) {
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
                for(Entity entity : Wrapper.INSTANCE.getWorld().getEntities())
                {
                    if(entity instanceof PlayerEntity)
                    {
                        PlayerEntity playerEntity = (PlayerEntity)entity;
                        if(playerEntity.isOnGround() && !touchedGround.contains(playerEntity))
                            touchedGround.add(playerEntity);
                        if(playerEntity.handSwingProgress > 0 && !swung.contains(playerEntity))
                            swung.add(playerEntity);
                    }
                }
                for(int i = 0; i < swung.size() - 1; i++)
                {
                    PlayerEntity playerEntity = swung.get(i);
                    if(playerEntity == null)
                    {
                        swung.remove(i);
                    }
                }
                for(int i = 0; i < touchedGround.size() - 1; i++)
                {
                    PlayerEntity playerEntity = touchedGround.get(i);
                    if(playerEntity == null)
                    {
                        touchedGround.remove(i);
                    }
                }
            }
        }

        if (!mode.equalsIgnoreCase(lastMode) && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        setSuffix(mode + " : " + attackMode);
        lastMode = mode;
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

    public boolean isValid(Entity entity, boolean rangecheck) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        if (entity == Wrapper.INSTANCE.getLocalPlayer())
            return false;
        if (Wrapper.INSTANCE.getLocalPlayer().getVehicle() != null) {
            if (entity == Wrapper.INSTANCE.getLocalPlayer().getVehicle())
                return false;
        }
        if (((LivingEntity) entity).isSleeping())
            return false;
        if (rangecheck)
            if (!Wrapper.INSTANCE.getLocalPlayer().canSee(entity)) {
                if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > 3) {
                    return false;
                }
            } else {
                if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > reach) {
                    return false;
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
            return (!swung.contains(playerEntity) && !touchedGround.contains(playerEntity)) || playerEntity.getGameProfile().getProperties().isEmpty();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (BaritoneHelper.INSTANCE.baritoneExists())
            BaritoneHelper.INSTANCE.disableKillauraTargetProcess();
        FeatureExtension.get(mode, this).disable();
    }

}
