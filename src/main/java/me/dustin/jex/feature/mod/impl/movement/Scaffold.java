package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ConcurrentLinkedQueue;

@Feature.Manifest(name = "Scaffold", category = Feature.Category.MOVEMENT, description = "Place blocks under yourself automatically.")
public class Scaffold extends Feature {

    @Op(name = "Place Mode", all = {"Post", "Pre"})
    public String placeMode = "Post";
    @Op(name = "Delay", min = 0, max = 1000, inc = 10)
    public int delay = 50;
    @Op(name = "Sneak on Place")
    public boolean sneak = false;
    @Op(name = "Range", min = 0, max = 4)
    public int range = 0;
    BlockHitResult blockHitResult;
    private Timer timer = new Timer();
    private ConcurrentLinkedQueue<BlockInfo> emptyNearBlocks = new ConcurrentLinkedQueue<>();

    @EventListener(events = {EventPlayerPackets.class, EventWalkOffBlock.class})
    private void runMethod(Event event) {
        if (AutoEat.isEating)
            return;
        if (event instanceof EventPlayerPackets) {
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
                blockHitResult = null;
                BlockPos below = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getPos().x, Wrapper.INSTANCE.getLocalPlayer().getPos().y - 0.5, Wrapper.INSTANCE.getLocalPlayer().getPos().z);
                    if (AutoEat.isEating)
                        return;
                        getNearBlocks(below);
                    for (BlockInfo blockInfo : emptyNearBlocks) {
                        if (blockInfo != null) {
                            if (getBlockFromHotbar() == -1) {
                                if (InventoryHelper.INSTANCE.isHotbarFull()) {
                                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 44, SlotActionType.THROW);
                                }
                                if (getBlockFromInv() != -1) {
                                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), SlotActionType.QUICK_MOVE);
                                }
                            }
                            if (getBlockFromHotbar() != -1) {
                                InventoryHelper.INSTANCE.setSlot(getBlockFromHotbar(), true, true);
                                place(blockInfo, (EventPlayerPackets) event);
                                emptyNearBlocks.remove(blockInfo);
                                if (delay > 0 && placeMode.equalsIgnoreCase("Pre"))
                                    return;
                            }
                        }
                    }
            } else if (blockHitResult != null) {
                if (!timer.hasPassed(delay)) {
                    return;
                }
                if (placeMode.equalsIgnoreCase("Post"))
                    Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, blockHitResult);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                if (sneak) {
                    NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                }
                timer.reset();
            }
        } else if (event instanceof EventWalkOffBlock) {
            event.cancel();
        }
    }

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
        if (range == 0) {
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
        for (int x = -range - 1; x < range + 1; x++) {
            for (int z = -range - 1; z < range + 1; z++) {
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
        if (sneak) {
            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        BlockPos lookAtPos = blockInfo.blockpos();
        RotationVector rotation = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(lookAtPos.getX(), lookAtPos.getY(), lookAtPos.getZ()));
        event.setYaw(rotation.getYaw());
        event.setPitch(80);

        Wrapper.INSTANCE.getLocalPlayer().headYaw = event.getYaw();
        Wrapper.INSTANCE.getLocalPlayer().bodyYaw = event.getYaw();

        blockHitResult = new BlockHitResult(new Vec3d(blockInfo.blockpos().getX(), blockInfo.blockpos().getY(), blockInfo.blockpos().getZ()), blockInfo.facing(), blockInfo.blockpos(), false);
        if (placeMode.equalsIgnoreCase("Pre"))
            Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, blockHitResult);
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
        return blockItem.getBlock().getDefaultState().isFullCube(Wrapper.INSTANCE.getWorld(), BlockPos.ORIGIN);
    }

    public record BlockInfo (BlockPos blockpos, Direction facing) {}
}
