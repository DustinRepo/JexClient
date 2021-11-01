package me.dustin.jex.helper.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashSet;
import java.util.Set;

public enum MCPathFinder {//WIP, path is alway null for some reason
    INSTANCE;

    private SkeletonEntity dummy;
    private MobNavigation mobNavigation;
    private Path path;

    private BlockPos goal;

    public void setGoal(BlockPos blockPos) {
        goal = blockPos;
    }
    
    @EventListener(events = {EventPlayerPackets.class, EventJoinWorld.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets && eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (goal == null)
                return;
            if (dummy == null)
                dummy = new SkeletonEntity(EntityType.SKELETON, Wrapper.INSTANCE.getWorld());
            dummy.copyPositionAndRotation(Wrapper.INSTANCE.getLocalPlayer());
            if (mobNavigation == null)
                mobNavigation = new MobNavigation(dummy, Wrapper.INSTANCE.getWorld());
            path = mobNavigation.findPathTo(goal, (int)ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getVelocity(), ClientMathHelper.INSTANCE.getVec(goal)));
            JexClient.INSTANCE.getLogger().info(path);
            if (path == null)
                return;
            PathNode node = path.getCurrentNode();
            float yaw = getYaw(new Vec3d(node.x, node.y + 0.5D, node.z));
            double newx = Math.sin(yaw * 3.1415927F / 180.0) * 0.2570236135776357;
            double newz = Math.cos(yaw * 3.1415927F / 180.0) * 0.2570236135776357;
            if(Wrapper.INSTANCE.getLocalPlayer().isTouchingWater()){
                newx *= 0.4;
                newz *= 0.4;
            }
            PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getLocalPlayer().getVelocity().getX() - newx);
            PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getLocalPlayer().getVelocity().getZ() + newz);
            if ((Wrapper.INSTANCE.getLocalPlayer().horizontalCollision) && (Wrapper.INSTANCE.getLocalPlayer().isOnGround())) {
                Wrapper.INSTANCE.getLocalPlayer().jump();
            }
            if (((Wrapper.INSTANCE.getLocalPlayer().isTouchingWater()) || Wrapper.INSTANCE.getLocalPlayer().isInLava()) && !Wrapper.INSTANCE.getOptions().keySneak.isPressed() && !Wrapper.INSTANCE.getOptions().keyJump.isPressed()) {
                PlayerHelper.INSTANCE.setVelocityY(Wrapper.INSTANCE.getLocalPlayer().getVelocity().getY() + 0.039);
            }
        } else if (event instanceof EventJoinWorld) {
            mobNavigation = null;
            path = null;
            dummy = null;
            goal = null;
        }
    }

    private float getYaw(Vec3d pos)
    {
        double xD = Wrapper.INSTANCE.getLocalPlayer().getX() - pos.getX();
        double zD = Wrapper.INSTANCE.getLocalPlayer().getZ() - pos.getZ();
        double yaw = Math.atan2(zD, xD);
        return (float)Math.toDegrees(yaw) + 90.0F;
    }
}
