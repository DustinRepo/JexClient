package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.impl.world.Excavator;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathFinder;
import me.dustin.jex.helper.world.wurstpathfinder.PathProcessor;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

@Feature.Manifest(category = Feature.Category.MISC, description = "Prevent yourself from being detected as AFK and potentially kicked")
public class AntiAFK extends Feature {

    @Op(name = "Mode", all = {"Wander", "Swing", "Jump", "Chat"})
    public String mode = "Wander";

    @Op(name = "Timer (Seconds)", min = 5, max = 120, inc = 1)
    public int secondsDelay = 5;

    private final Timer timer = new Timer();

    private BlockPos afkSpot;
    private BlockPos lastSpot;
    private WanderPathFinder pathFinder;
    private PathProcessor pathProcessor;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        setSuffix(mode);
        if (afkSpot == null)
            afkSpot = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
        if (lastSpot == null)
            lastSpot = Wrapper.INSTANCE.getLocalPlayer().getBlockPos();
        if (timer.hasPassed(secondsDelay * 1000L)) {
            switch (mode) {
                case "Swing":
                    Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                    break;
                case "Jump":
                    if (Wrapper.INSTANCE.getLocalPlayer().isOnGround())
                        Wrapper.INSTANCE.getLocalPlayer().jump();
                    break;
                case "Walk":
                    NetworkHelper.INSTANCE.sendPacket(new ChatMessageC2SPacket(Wrapper.INSTANCE.getLocalPlayer().age + ""));
                    break;
                case "Wander":
                    pathFinder = new WanderPathFinder(afkSpot,this);
                    break;
            }
            timer.reset();
        }
        if (pathFinder != null) {
            if (!pathFinder.isDone() && !pathFinder.isFailed()) {
                PathProcessor.lockControls();
                pathFinder.think();

                if (!pathFinder.isDone() && !pathFinder.isFailed())
                    return;

                pathFinder.formatPath();
                pathProcessor = pathFinder.getProcessor();
            }

            if (pathProcessor != null && !pathFinder.isPathStillValid(pathProcessor.getIndex())) {
                pathFinder = new WanderPathFinder(afkSpot,this);
                return;
            }

            if (pathProcessor == null) {
                PathProcessor.releaseControls();
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
       if (pathFinder != null && pathFinder.isDone()) {
           pathFinder.renderPath(event.getMatrixStack(), false, false);
       }
    });

    @Override
    public void onDisable() {
        afkSpot = null;
        lastSpot = null;
        PathProcessor.releaseControls();
        super.onDisable();
    }

    private static class WanderPathFinder extends PathFinder {

        private final AntiAFK antiAFK;

        public WanderPathFinder(BlockPos goal, AntiAFK antiAFK) {
            super(goal);
            setThinkTime(10);
            this.antiAFK = antiAFK;
        }

        @Override
        public boolean checkDone() {
            //more than 5 blocks away from current player position and no more than 15 blocks away from the start position and 3 or more blocks away from the last position so it doesn't loop two spots
            Vec3d currentVec = Vec3d.of(current);
            double playerDistance = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), currentVec);
            double origSpotDistance = ClientMathHelper.INSTANCE.getDistance(Vec3d.of(getGoal()), currentVec);
            double lastSpotDistance = ClientMathHelper.INSTANCE.getDistance(Vec3d.of(antiAFK.lastSpot), currentVec);

            done = playerDistance > 5 && origSpotDistance < 15 && origSpotDistance > 4 && lastSpotDistance > 6;
            if (done) {
                antiAFK.lastSpot = current;
            }
            return done;
        }
    }
}
