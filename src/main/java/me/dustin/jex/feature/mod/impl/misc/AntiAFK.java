package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.world.PathingHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.helper.world.wurstpathfinder.PathFinder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Feature.Manifest(category = Feature.Category.MISC, description = "Prevent yourself from being detected as AFK and potentially kicked")
public class AntiAFK extends Feature {

    @Op(name = "Mode", all = {"Wander", "Swing", "Jump", "Chat"})
    public String mode = "Wander";

    @Op(name = "Timer (Seconds)", min = 5, max = 120, inc = 1)
    public int secondsDelay = 5;

    private final StopWatch stopWatch = new StopWatch();

    private BlockPos afkSpot;
    private BlockPos[] lastSpots;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        setSuffix(mode);
        if (afkSpot == null)
            afkSpot = Wrapper.INSTANCE.getPlayer().getBlockPos();
        if (lastSpots == null)
            lastSpots = new BlockPos[]{Wrapper.INSTANCE.getPlayer().getBlockPos(), Wrapper.INSTANCE.getPlayer().getBlockPos()};
        if (stopWatch.hasPassed(secondsDelay * 1000L)) {
            switch (mode) {
                case "Swing":
                    Wrapper.INSTANCE.getPlayer().swingHand(Hand.MAIN_HAND);
                    break;
                case "Jump":
                    if (Wrapper.INSTANCE.getPlayer().isOnGround())
                        Wrapper.INSTANCE.getPlayer().jump();
                    break;
                case "Chat":
                    ChatHelper.INSTANCE.sendChatMessage(Wrapper.INSTANCE.getPlayer().age + "");
                    break;
                case "Wander":
                    PathingHelper.INSTANCE.setAllowMining(false);
                    PathingHelper.INSTANCE.setPathFinder(new WanderPathFinder(afkSpot, this));
                    break;
            }
            stopWatch.reset();
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onDisable() {
        afkSpot = null;
        lastSpots = null;
        PathingHelper.INSTANCE.cancelPathing();
        super.onDisable();
    }

    private static class WanderPathFinder extends PathFinder {

        private final AntiAFK antiAFK;

        public WanderPathFinder(BlockPos goal, AntiAFK antiAFK) {
            super(goal);
            setThinkTime(10);
            setFallingAllowed(false);
            setDivingAllowed(false);
            this.antiAFK = antiAFK;
        }

        @Override
        public boolean checkDone() {
            //more than 5 blocks away from current player position and no more than 15 blocks away from the start position and 3 or more blocks away from the last position so it doesn't loop two spots
            Vec3d currentVec = Vec3d.of(current);
            double playerDistance = ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos(), currentVec);
            double origSpotDistance = ClientMathHelper.INSTANCE.getDistance(Vec3d.of(getGoal()), currentVec);
            double lastSpotDistance = ClientMathHelper.INSTANCE.getDistance(Vec3d.of(antiAFK.lastSpots[0]), currentVec);

            Block below = WorldHelper.INSTANCE.getBlock(current.down());

            done = below != Blocks.AIR && playerDistance > 5 && origSpotDistance < 15 && origSpotDistance > 4 && lastSpotDistance > 6;
            if (done) {
                antiAFK.lastSpots[0] = antiAFK.lastSpots[1];
                antiAFK.lastSpots[1] = current;
            }
            return done;
        }
    }
}
