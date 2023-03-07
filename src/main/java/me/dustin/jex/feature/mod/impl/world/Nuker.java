package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Nuker extends Feature {
    public Nuker() {
        super(Category.WORLD, "Nuke the blocks around you.");
    }

    private final StopWatch stopWatch = new StopWatch();

    public final Property<Integer> distanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Distance")
            .value(4)
            .min(3)
            .max(6)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Break Delay")
            .value(0L)
            .max(1000L)
            .inc(10L)
            .build();
    public final Property<Boolean> keepFloorProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Keep Floor")
            .value(true)
            .build();
    public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();
	

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        ArrayList<BlockPos> positions = getPositions();
        positions.forEach(blockPos -> {
            new EventClickBlock(blockPos, Direction.UP, EventClickBlock.Mode.PRE).run();
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
            NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
              if (swingProperty.value()) {
            Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
	      }
            stopWatch.reset();
        });
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    public ArrayList<BlockPos> getPositions() {
        ArrayList<BlockPos> blockPosList = new ArrayList<>();
        int dist = distanceProperty.value();
        int minY = keepFloorProperty.value() ? 0 : -dist;
	  for (int x = dist; x > -dist; x--)
	    for (int z = -dist; z < dist; z++)
                for (int y = minY; y < dist; y++)  {
                    BlockPos pos = Wrapper.INSTANCE.getPlayer().getBlockPos().add(x, y, z);
                    Block block = WorldHelper.INSTANCE.getBlock(pos);
                    if (!(block instanceof AirBlock || block instanceof FluidBlock)) {
                        double distance = ClientMathHelper.INSTANCE.getDistance(Vec3d.ofCenter(pos), Wrapper.INSTANCE.getPlayer().getPos().add(0, 0, 0));
                        if (distance > distanceProperty.value())
                            continue;
                        blockPosList.add(pos);
                        if (delayProperty.value() > 0)
                            break;
                    }
                }
        blockPosList.sort(Comparator.comparing(o -> o.getSquaredDistance(Wrapper.INSTANCE.getPlayer().getPos())));
        return blockPosList;
    }
}
