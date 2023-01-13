package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Scaffold extends Feature {

    public final Property<PlaceTiming> placeModeProperty = new Property.PropertyBuilder<PlaceTiming>(this.getClass())
            .name("Place Mode")
            .value(PlaceTiming.POST)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay")
            .value(50L)
            .max(1000)
            .inc(10)
            .build();
    public final Property<Boolean> sneakProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Sneak on Place")
            .description("Sneak when you place a block.")
            .value(false)
            .build();
    public final Property<Integer> xrangeProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RangeX")
            .value(0)
            .max(4)
            .build();
    public final Property<Integer> zrangeProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("RangeZ")
            .value(0)
            .max(4)
            .build();

    private BlockHitResult blockHitResult;
    private final StopWatch stopWatch = new StopWatch();
    private final ConcurrentLinkedQueue<BlockInfo> emptyNearBlocks = new ConcurrentLinkedQueue<>();

    public Scaffold() {
        super(Category.MOVEMENT, "Place blocks under yourself automatically.");
    }

    @EventPointer
    private final EventListener<EventWalkOffBlock> eventWalkOffBlockEventListener = new EventListener<>(event -> event.cancel());

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating)
            return;
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            blockHitResult = null;
            BlockPos below = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getPos().x, Wrapper.INSTANCE.getLocalPlayer().getPos().y - 0.5, Wrapper.INSTANCE.getLocalPlayer().getPos().z);
            if (AutoEat.isEating)
                return;
            getNearBlocks(below);
            for (BlockInfo blockInfo : emptyNearBlocks) {
                if (blockInfo != null) {
                    if (getBlockFromHotbar() == -1)
                        if (getBlockFromInv() != -1) {
                            InventoryHelper.INSTANCE.swapToHotbar(getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), 8);
                        }
                    if (getBlockFromHotbar() != -1) {
                        InventoryHelper.INSTANCE.setSlot(getBlockFromHotbar(), true, true);
                        place(blockInfo, event);
                        emptyNearBlocks.remove(blockInfo);
                        if (delayProperty.value() > 0 && placeModeProperty.value() == PlaceTiming.PRE)
                            return;
                    }
                }
            }
        } else if (blockHitResult != null) {
            if (!stopWatch.hasPassed(delayProperty.value())) {
                return;
            }
            if (placeModeProperty.value() == PlaceTiming.POST)
                PlayerHelper.INSTANCE.placeBlockInPos(blockHitResult.getBlockPos(), Hand.MAIN_HAND, false);

            if (sneakProperty.value())
                NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            stopWatch.reset();
        }
    });

    private boolean isReplaceable(Block block) {
        return block.getDefaultState().getMaterial().isReplaceable();
    }

    private boolean goingToPlace(BlockPos blockPos) {
        for (BlockInfo blockInfo : emptyNearBlocks) {
            if (isEqual(blockPos, blockInfo.blockpos()))
                return true;
        }
        return false;
    }

    private boolean isEqual(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
    }

    private void getNearBlocks(BlockPos blockPos) {
        emptyNearBlocks.clear();
        if (xrangeProperty.value(),zrangeProperty.value() == 0) {
            BlockPos below = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getPos().x, Wrapper.INSTANCE.getLocalPlayer().getPos().y - 0.5, Wrapper.INSTANCE.getLocalPlayer().getPos().z);
            if (!isReplaceable(WorldHelper.INSTANCE.getBlock(below)))
                return;
            BlockInfo blockInfo = getBlockInfo(blockPos);
            if (blockInfo == null) {
                blockInfo = getBlockInfo(blockPos.add(1, 0, 0));
                if (blockInfo == null) {
                    blockInfo = getBlockInfo(blockPos.add(-1, 0, 0));
                    if (blockInfo == null) {
                        blockInfo = getBlockInfo(blockPos.add(0, 0, 1));
                        if (blockInfo == null) {
                            blockInfo = getBlockInfo(blockPos.add(0, 0, -1));
                        }
                    }
                }
            }
            if (blockInfo != null) {
                emptyNearBlocks.offer(blockInfo);
                if (blockInfo.blockpos() != blockPos)
                    emptyNearBlocks.offer(new BlockInfo(blockPos, null));
            }
            return;
        }
        for (int x = -xrangeProperty.value() - 1; x < xrangeProperty.value() + 1; x++) {
            for (int z = -zrangeProperty.value() - 1; z < zrangeProperty.value() + 1; z++) {
                BlockPos blockPos1 = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getPos().add(0, -0.5f, 0)).add(x, 0, z);
                if (isReplaceable(WorldHelper.INSTANCE.getBlock(blockPos1)) || goingToPlace(blockPos1)) {
                    BlockInfo blockInfo = getBlockInfo(blockPos1);
                    if (blockInfo != null)
                        emptyNearBlocks.offer(blockInfo);
                }
            }
        }
    }

    public void place(BlockInfo blockInfo, EventPlayerPackets event) {
        if (blockInfo.facing() == null) {
            blockInfo = getBlockInfo(blockInfo.blockpos());
        }
        if (blockInfo == null)
            return;
        if (sneakProperty.value()) {
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        BlockPos lookAtPos = blockInfo.blockpos();
        RotationVector rotation = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(lookAtPos.getX(), lookAtPos.getY(), lookAtPos.getZ()));
        event.setYaw(rotation.getYaw());
        event.setPitch(80);

        Wrapper.INSTANCE.getLocalPlayer().headYaw = event.getYaw();
        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = event.getYaw();

        blockHitResult = new BlockHitResult(new Vec3d(blockInfo.blockpos().getX(), blockInfo.blockpos().getY(), blockInfo.blockpos().getZ()), blockInfo.facing(), blockInfo.blockpos(), false);
        if (placeModeProperty.value() == PlaceTiming.PRE)
            PlayerHelper.INSTANCE.placeBlockInPos(blockInfo.blockpos(), Hand.MAIN_HAND, false);
    }

    public BlockInfo getBlockInfo(BlockPos pos) {
        BlockPos down = pos.down();
        BlockPos north = pos.north();
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        if (!isReplaceable(WorldHelper.INSTANCE.getBlock(down))) {
            return new BlockInfo(pos, Direction.UP);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(north))) {
            return new BlockInfo(pos, Direction.SOUTH);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(east))) {
            return new BlockInfo(pos, Direction.WEST);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(south))) {
            return new BlockInfo(pos, Direction.NORTH);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(west))) {
            return new BlockInfo(pos, Direction.EAST);
        }
        return null;
    }

    public int getBlockFromInv() {
        for (int i = 0; i < 36; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() instanceof BlockItem)
                if (shouldUse((BlockItem) InventoryHelper.INSTANCE.getInventory().getStack(i).getItem()))
                    return i;
        }
        return -1;
    }

    public int getBlockFromHotbar() {
        for (int i = 0; i < 9; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() instanceof BlockItem)
                if (shouldUse((BlockItem) InventoryHelper.INSTANCE.getInventory().getStack(i).getItem()))
                    return i;
        }
        return -1;
    }

    public boolean shouldUse(BlockItem blockItem) {
        return blockItem.getBlock().getDefaultState().hasSolidTopSurface(Wrapper.INSTANCE.getWorld(), BlockPos.ORIGIN, Wrapper.INSTANCE.getLocalPlayer()) && blockItem.getBlock().getDefaultState().onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS;
    }

    public record BlockInfo (BlockPos blockpos, Direction facing) {}

    public enum PlaceTiming {
        PRE, POST
    }
}
