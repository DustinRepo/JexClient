package bedrockminer.utils;

import java.util.ArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import static net.minecraft.world.level.block.Block.canSupportCenter;

public class CheckingEnvironment {

    public static BlockPos findNearbyFlatBlockToPlaceRedstoneTorch(ClientLevel world, BlockPos blockPos) {
        if ((canSupportCenter(world, blockPos.east(), Direction.UP) && (world.getBlockState(blockPos.east().above()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.east().above()).is(Blocks.REDSTONE_TORCH))) {
            return blockPos.east();
        } else if ((canSupportCenter(world, blockPos.west(), Direction.UP) && (world.getBlockState(blockPos.west().above()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.west().above()).is(Blocks.REDSTONE_TORCH))) {
            return blockPos.west();
        } else if ((canSupportCenter(world, blockPos.north(), Direction.UP) && (world.getBlockState(blockPos.north().above()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.north().above()).is(Blocks.REDSTONE_TORCH))) {
            return blockPos.north();
        } else if ((canSupportCenter(world, blockPos.south(), Direction.UP) && (world.getBlockState(blockPos.south().above()).getMaterial().isReplaceable()) || world.getBlockState(blockPos.south().above()).is(Blocks.REDSTONE_TORCH))) {
            return blockPos.south();
        }
        return null;
    }

    public static BlockPos findPossibleSlimeBlockPos(ClientLevel world, BlockPos blockPos) {
        if (world.getBlockState(blockPos.east()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.east().above()).getMaterial().isReplaceable())) {
            return blockPos.east();
        } else if (world.getBlockState(blockPos.west()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.west().above()).getMaterial().isReplaceable())) {
            return blockPos.west();
        } else if (world.getBlockState(blockPos.south()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.south().above()).getMaterial().isReplaceable())) {
            return blockPos.south();
        } else if (world.getBlockState(blockPos.north()).getMaterial().isReplaceable() && (world.getBlockState(blockPos.north().above()).getMaterial().isReplaceable())) {
            return blockPos.north();
        }
        return null;
    }

    public static boolean has2BlocksOfPlaceToPlacePiston(ClientLevel world, BlockPos blockPos) {
        if (world.getBlockState(blockPos.above()).getDestroySpeed(world, blockPos.above()) == 0) {
            BlockBreaker.breakBlock(world, blockPos.above());
        }
        return world.getBlockState(blockPos.above()).getMaterial().isReplaceable() && world.getBlockState(blockPos.above().above()).getMaterial().isReplaceable();
    }

    public static ArrayList<BlockPos> findNearbyRedstoneTorch(ClientLevel world, BlockPos pistonBlockPos) {
        ArrayList<BlockPos> list = new ArrayList<>();
        if (world.getBlockState(pistonBlockPos.east()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.east());
        }
        if (world.getBlockState(pistonBlockPos.west()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.west());
        }
        if (world.getBlockState(pistonBlockPos.south()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.south());
        }
        if (world.getBlockState(pistonBlockPos.north()).is(Blocks.REDSTONE_TORCH)) {
            list.add(pistonBlockPos.north());
        }
        return list;
    }
}
