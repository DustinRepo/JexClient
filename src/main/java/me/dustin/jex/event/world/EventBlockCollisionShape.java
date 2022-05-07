package me.dustin.jex.event.world;

import me.dustin.events.core.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EventBlockCollisionShape extends Event {

    private final BlockPos blockPos;
    private final Block block;
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
