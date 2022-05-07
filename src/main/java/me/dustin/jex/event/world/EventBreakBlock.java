package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EventBreakBlock extends Event {
    private final BlockState blockState;
    private final BlockPos pos;

    public EventBreakBlock(BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.pos = blockPos;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public BlockPos getPos() {
        return pos;
    }
}
