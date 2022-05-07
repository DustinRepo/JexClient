package me.dustin.jex.helper.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.impl.player.Freecam;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.wurstpathfinder.PathFinder;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public enum PathingHelper {
    INSTANCE;

    private PathFinder pathFinder;
    private PathProcessor pathProcessor;

    private boolean allowMining;

    private BlockPos goal;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
       if (pathFinder != null) {
           if (!pathFinder.isDone() && !pathFinder.isFailed()) {
               pathFinder.think();
               if (Wrapper.INSTANCE.getPlayer() != Freecam.playerEntity)
                   PathProcessor.lockControls();
               else
                   PathProcessor.releaseControls();
               if (!pathFinder.isDone() && !pathFinder.isFailed())
                   return;
               pathFinder.formatPath();
               pathProcessor = pathFinder.getProcessor();
           }

           if (pathProcessor != null && !pathFinder.isPathStillValid(pathProcessor.getIndex()) && goal != null) {
               pathFinder = new PathFinder(pathFinder);
               return;
           }

           pathProcessor.process();

           if (pathProcessor.isDone()) {
               pathFinder = null;
               pathProcessor = null;
               PathProcessor.releaseControls();
           }
       }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (pathFinder != null)
            pathFinder.renderPath(event.getPoseStack(), false, true);
    });

    public void pathTo(BlockPos blockPos) {
        this.goal = blockPos;
        this.pathFinder = new PathFinder(blockPos);
    }

    public void pathNear(BlockPos blockPos, int range) {
        this.goal = blockPos;
        this.pathFinder = new NearPathFinder(blockPos, range);
        this.pathFinder.setCanMine(allowMining);
    }

    public void pathNear(BlockPos blockPos, int range, int thinkTime) {
        this.goal = blockPos;
        this.pathFinder = new NearPathFinder(blockPos, range);
        this.pathFinder.setThinkTime(thinkTime);
        this.pathFinder.setCanMine(allowMining);
    }

    public void pathTo(BlockPos blockPos, int thinkTime) {
        this.goal = blockPos;
        this.pathFinder = new PathFinder(blockPos);
        this.pathFinder.setThinkTime(thinkTime);
        this.pathFinder.setCanMine(allowMining);
    }

    public void cancelPathing() {
        this.goal = null;
        this.pathFinder = null;
        this.pathProcessor = null;
        PathProcessor.releaseControls();
    }

    public boolean isPathing() {
        return this.pathFinder != null;
    }

    public boolean isThinking() {
        return isPathing() && !pathFinder.isDone() && !pathFinder.isFailed();
    }

    public void setAllowMining(boolean allowMining) {
        this.allowMining = allowMining;
    }

    public PathFinder getPathFinder() {
        return this.pathFinder;
    }

    public void setPathFinder(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
        this.goal = pathFinder.getGoal();
        this.pathProcessor = null;
    }

    private static class NearPathFinder extends PathFinder{
        private final int range;

        public NearPathFinder(BlockPos goal, int range) {
            super(goal);
            setThinkTime(10);
            setCanMine(true);
            this.range = range;
        }

        @Override
        protected boolean checkDone() {
            return done = WorldHelper.INSTANCE.getBlockState(current.below()).getCollisionShape(Wrapper.INSTANCE.getWorld(), current.below()) != Shapes.empty() && ClientMathHelper.INSTANCE.getDistance(Vec3.atLowerCornerOf(getGoal()), Vec3.atLowerCornerOf(current)) <= this.range;
        }
    }

}
