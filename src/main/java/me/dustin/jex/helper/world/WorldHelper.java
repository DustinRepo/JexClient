package me.dustin.jex.helper.world;

import com.google.common.collect.Maps;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

public enum WorldHelper {
    INSTANCE;
    private ConcurrentMap<BlockPos, BlockEntity> blockEntities = Maps.newConcurrentMap();

    public Block getBlock(BlockPos pos) {
        if (Wrapper.INSTANCE.getWorld() == null)
            return null;
        return Wrapper.INSTANCE.getWorld().getBlockState(pos).getBlock();
    }

    public BlockState getBlockState(BlockPos pos) {
        if (Wrapper.INSTANCE.getWorld() == null)
            return null;
        return Wrapper.INSTANCE.getWorld().getBlockState(pos);
    }

    public Identifier getDimensionID() {
        return Wrapper.INSTANCE.getWorld().getRegistryKey().getValue();
    }

    public Block getBlockBelowEntity(Entity entity, float offset) {
        if (Wrapper.INSTANCE.getWorld() == null || entity == null)
            return null;

        BlockPos blockPos = new BlockPos(entity.getPos().getX(), entity.getPos().getY() - offset, entity.getPos().getZ());
        return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getBlock();
    }

    public Block getBlockAboveEntity(Entity entity, float offset) {
        if (Wrapper.INSTANCE.getWorld() == null || entity == null)
            return null;

        BlockPos blockPos = new BlockPos(entity.getPos().getX(), entity.getPos().getY() + entity.getHeight() + offset, entity.getPos().getZ());
        return Wrapper.INSTANCE.getWorld().getBlockState(blockPos).getBlock();
    }

    public Block getBlockAboveEntity(Entity entity) {
        return getBlockAboveEntity(entity, -2.5f);
    }

    public Block getBlockBelowEntity(Entity entity) {
        return getBlockBelowEntity(entity, 0.5f);
    }

    public void removeBlockEntity(BlockPos pos) {
        blockEntities.remove(pos);
    }

    public ConcurrentMap<BlockPos, BlockEntity> getBlockEntityList() {
        return blockEntities;
    }

    @EventListener(events={EventTick.class})
    private void runMethod(EventTick eventTick) {
        if (Wrapper.INSTANCE.getWorld() == null)
            blockEntities.clear();
        else {
            Iterator<BlockEntity> it = blockEntities.values().iterator();
            while (it.hasNext()) {
                BlockEntity blockEntity = it.next();
                if (Wrapper.INSTANCE.getWorld().getBlockEntity(blockEntity.getPos()) == null)
                    removeBlockEntity(blockEntity.getPos());
            }
        }
    }

    public Collection<BlockEntity> getBlockEntities() {
        return blockEntities.values();
    }

    public boolean isOnLiquid(Entity entity) {
        if (entity == null) {
            return false;
        }
        Box boundingBox = entity.getBoundingBox();
        boundingBox = boundingBox.expand(-0.01D, -0.0D, -0.01D).offset(0.0D, -0.01D, 0.0D);
        boolean onLiquid = false;
        int y = (int) boundingBox.minY;
        for (int x = MathHelper.floor(boundingBox.minX); x < MathHelper.floor(boundingBox.maxX + 1.0D); x++) {
            for (int z = MathHelper.floor(boundingBox.minZ); z <
                    MathHelper.floor(boundingBox.maxZ + 1.0D); z++) {
                Block block = getBlock(new BlockPos(x, y, z));
                if (block != Blocks.AIR) {
                    if (!(block instanceof FluidBlock)) {
                        return false;
                    }
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    public boolean isInLiquid(Entity entity) {
        if (entity == null) {
            return false;
        }
        Box par1AxisAlignedBB = entity.getBoundingBox();
        par1AxisAlignedBB = par1AxisAlignedBB.expand(-0, -0.081D, -0.081D);
        int var4 = MathHelper.floor(par1AxisAlignedBB.minX);
        int var5 = MathHelper.floor(par1AxisAlignedBB.maxX + 1.0D);
        int var6 = MathHelper.floor(par1AxisAlignedBB.minY);
        int var7 = MathHelper.floor(par1AxisAlignedBB.maxY + 0.8D);
        int var8 = MathHelper.floor(par1AxisAlignedBB.minZ);
        int var9 = MathHelper.floor(par1AxisAlignedBB.maxZ + 1.0D);
        if (Wrapper.INSTANCE.getWorld().getChunk(
                new BlockPos(entity.getX(), entity.getY(), entity.getZ())) == null) {
            return false;
        }
        for (int var12 = var4; var12 < var5; var12++) {
            for (int var13 = var6; var13 < var7; var13++) {
                for (int var14 = var8; var14 < var9; var14++) {
                    Block var15 = getBlock(new BlockPos(var12, var13, var14));
                    if ((var15 instanceof FluidBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
