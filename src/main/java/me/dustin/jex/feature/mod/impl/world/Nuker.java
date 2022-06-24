package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Nuker extends Feature {
    public Nuker() {
        super(Category.WORLD, "Nuke the blocks around you. (Creative mode only)");
    }

    private final StopWatch stopWatch = new StopWatch();

    public final Property<Integer> distanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Distance")
            .description("The distance at which to break blocks")
            .value(4)
            .min(3)
            .max(7)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Break Delay")
            .description("Delay between breaking blocks in milliseconds")
            .value(0L)
            .max(500)
            .inc(10)
            .build();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (!Wrapper.INSTANCE.getLocalPlayer().getAbilities().creativeMode) {
            ChatHelper.INSTANCE.addClientMessage("Nuker can only be used in creative!");
            setState(false);
            return;
        }
        if (!stopWatch.hasPassed(delayProperty.value()))
            return;
        for (int x = -distanceProperty.value(); x < distanceProperty.value(); x++)
            for (int y = -distanceProperty.value(); y < distanceProperty.value(); y++)
                for (int z = -distanceProperty.value(); z < distanceProperty.value(); z++) {
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    Block block = WorldHelper.INSTANCE.getBlock(pos);
                    if (!(block instanceof AirBlock || block instanceof FluidBlock)) {
                        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
                        NetworkHelper.INSTANCE.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                        stopWatch.reset();
                        if (delayProperty.value() > 0)
                            return;
                    }
                }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
