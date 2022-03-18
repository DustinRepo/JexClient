package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class EventBlockCollisionShape extends Event {

    private BlockPos blockPos;
    private Block block;
    private VoxelShape voxelShape;

    public EventBlockCollisionShape(BlockPos blockPos, Block block, VoxelShape voxelShape) {
        this.blockPos = blockPos;
        this.block = block;
        this.voxelShape = voxelShape;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Block getBlock() {
        return block;
    }

    public VoxelShape getVoxelShape() {
        return voxelShape;
    }

    public void setVoxelShape(VoxelShape voxelShape) {
        this.voxelShape = voxelShape;
    }
}
