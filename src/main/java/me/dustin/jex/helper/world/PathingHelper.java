package me.dustin.jex.helper.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.world.wurstpathfinder.PathFinder;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

public enum PathingHelper {
    INSTANCE;

    private PathFinder pathFinder;
    private PathProcessor pathProcessor;

    private BlockPos goal;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
       if (pathFinder != null) {
           if (!pathFinder.isDone() && !pathFinder.isFailed()) {
               pathFinder.think();
               PathProcessor.lockControls();

               if (!pathFinder.isDone() && !pathFinder.isFailed())
                   return;
               pathFinder.formatPath();
               pathProcessor = pathFinder.getProcessor();
           }

           if (pathProcessor != null && !pathFinder.isPathStillValid(pathProcessor.getIndex()) && goal != null) {
               pathFinder = new PathFinder(goal);
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
            pathFinder.renderPath(event.getMatrixStack(), false, false);
    });

    public void pathTo(BlockPos blockPos) {
        this.goal = blockPos;
        this.pathFinder = new PathFinder(blockPos);
    }

    public void pathNear(BlockPos blockPos, int range) {
        this.goal = blockPos;
        this.pathFinder = new NearPathFinder(blockPos, range);
    }

    public void pathNear(BlockPos blockPos, int range, int thinkTime) {
        this.goal = blockPos;
        this.pathFinder = new NearPathFinder(blockPos, range);
        this.pathFinder.setThinkTime(thinkTime);
    }

    public void pathTo(BlockPos blockPos, int thinkTime) {
        this.goal = blockPos;
        this.pathFinder = new PathFinder(blockPos);
        this.pathFinder.setThinkTime(thinkTime);
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
        return isPathing() && !pathFinder.isDone() && pathFinder.isFailed();
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
            this.range = range;
        }

        @Override
        protected boolean checkDone() {
            return done = WorldHelper.INSTANCE.getBlockState(current.down()).getCollisionShape(Wrapper.INSTANCE.getWorld(), current.down()) != VoxelShapes.empty() && ClientMathHelper.INSTANCE.getDistance(Vec3d.of(getGoal()), Vec3d.of(current)) <= this.range;
        }
    }

}
