package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EventShouldDrawSide extends Event {

    private Block block;
    private Direction side;
    private BlockPos blockPos;
    private boolean shouldDrawSide;

    public EventShouldDrawSide(Block block, Direction side, BlockPos blockPos) {
        this.block = block;
        this.side = side;
        this.blockPos = blockPos;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public boolean isShouldDrawSide() {
        return shouldDrawSide;
    }

    public void setShouldDrawSide(boolean shouldDrawSide) {
        this.shouldDrawSide = shouldDrawSide;
    }

    public Direction getSide() {
        return side;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
