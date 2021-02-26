package me.dustin.jex.event.render;
/*
 * @Author Dustin
 * 9/29/2019
 */

import me.dustin.events.core.Event;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class EventShouldDrawSide extends Event {

    private Block block;
    private BlockPos blockPos;
    private boolean shouldDrawSide;

    public EventShouldDrawSide(Block block, BlockPos blockPos) {
        this.block = block;
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

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
