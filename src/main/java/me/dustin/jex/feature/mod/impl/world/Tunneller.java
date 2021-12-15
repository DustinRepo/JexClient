package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.Comparator;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Automatically dig tunnels")
public class Tunneller extends Feature {

    @Op(name = "Handle Liquids")
    public boolean handleLiquids = true;
    @Op(name = "Width", min = 1, max = 5)
    public int width = 3;
    @Op(name = "Height", min = 1, max = 5)
    public int height = 3;

    private Direction direction;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating)
            return;
        if (direction == null)
            direction = Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing();
        setSuffix(getDirectionString());
        //do liquid replacing
        if (handleLiquids)
            for (BlockPos liquidCheckSpot : getLiquidCheckSpots()) {
                if (WorldHelper.INSTANCE.getBlock(liquidCheckSpot) instanceof FluidBlock) {
                    if (moveToBlocks()) {
                        PlayerHelper.INSTANCE.placeBlockInPos(liquidCheckSpot, Hand.MAIN_HAND, true);
                        return;
                    } else {
                        ChatHelper.INSTANCE.addClientMessage("Tunneller ran out of blocks. Disabling.");
                        this.setState(false);
                        return;
                    }
                }
            }
        //break-a da blocks
        for (BlockPos blockPos : getBlocksInTunnel()) {
            if (WorldHelper.INSTANCE.getBlockState(blockPos).getOutlineShape(Wrapper.INSTANCE.getWorld(), blockPos) != VoxelShapes.empty()) {
                Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(blockPos, Direction.UP);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
                return;
            }
        }
        //make sure floor is there
        Box tunnelBox = getTunnelBox();
        Box floorBox = new Box(tunnelBox.minX, tunnelBox.minY - 1, tunnelBox.minZ, tunnelBox.maxX, tunnelBox.minY - 1, tunnelBox.maxZ);
        ArrayList<BlockPos> floorBlocks = WorldHelper.INSTANCE.getBlocksInBox(floorBox);
        for (BlockPos floorBlock : floorBlocks) {
            if (WorldHelper.INSTANCE.getBlockState(floorBlock).getOutlineShape(Wrapper.INSTANCE.getWorld(), floorBlock) == VoxelShapes.empty()) {
                if (moveToBlocks()) {
                    PlayerHelper.INSTANCE.placeBlockInPos(floorBlock, Hand.MAIN_HAND, true);
                } else {
                    ChatHelper.INSTANCE.addClientMessage("Tunneller ran out of blocks. Disabling.");
                    this.setState(false);
                }
                return;
            }
        }
        //move forward until one of the above catches
        switch (direction) {
            case NORTH -> PlayerHelper.INSTANCE.setVelocityZ(-moveSpeed());
            case SOUTH -> PlayerHelper.INSTANCE.setVelocityZ(moveSpeed());
            case WEST -> PlayerHelper.INSTANCE.setVelocityX(-moveSpeed());
            case EAST -> PlayerHelper.INSTANCE.setVelocityX(moveSpeed());
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            direction = Wrapper.INSTANCE.getLocalPlayer().getHorizontalFacing();
            PlayerHelper.INSTANCE.centerPerfectlyOnBlock();
        }
        super.onEnable();
    }

    private boolean moveToBlocks() {
        if (getBlockFromHotbar() == -1) {
            if (getBlockFromInv() != -1) {
                if (InventoryHelper.INSTANCE.isHotbarFull()) {
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 44, SlotActionType.THROW);
                }
                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), SlotActionType.QUICK_MOVE);
            }
        }
        int hotBarslot = getBlockFromHotbar();
        if (hotBarslot != -1) {
            InventoryHelper.INSTANCE.setSlot(hotBarslot, true, true);
            return true;
        }
        return false;
    }

    public int getBlockFromInv() {
        for (int i = 0; i < 36; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() instanceof BlockItem blockItem)
                if (blockItem.getBlock().getDefaultState().isFullCube(Wrapper.INSTANCE.getWorld(), BlockPos.ORIGIN))
                    return i;
        }
        return -1;
    }

    public int getBlockFromHotbar() {
        for (int i = 0; i < 9; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() instanceof BlockItem blockItem)
                if (blockItem.getBlock().getDefaultState().isFullCube(Wrapper.INSTANCE.getWorld(), BlockPos.ORIGIN))
                    return i;
        }
        return -1;
    }

    private ArrayList<BlockPos> getLiquidCheckSpots() {
        Box box = getTunnelBox().expand(1);
        ArrayList<BlockPos> blocks = WorldHelper.INSTANCE.getBlocksInBox(box);
        blocks.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), Vec3d.ofCenter(value))));
        blocks.sort(Comparator.comparingInt(value -> -value.getY()));
        return blocks;
    }

    private ArrayList<BlockPos> getBlocksInTunnel() {
        Box box = getTunnelBox();
        ArrayList<BlockPos> blocks = WorldHelper.INSTANCE.getBlocksInBox(box);
        blocks.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), Vec3d.ofCenter(value))));
        blocks.sort(Comparator.comparingInt(value -> -value.getY()));
        return blocks;
    }

    private Box getTunnelBox() {
        Box box = new Box(Wrapper.INSTANCE.getLocalPlayer().getBlockX() - width / 2, Wrapper.INSTANCE.getLocalPlayer().getBlockY(), Wrapper.INSTANCE.getLocalPlayer().getBlockZ() - width / 2, Wrapper.INSTANCE.getLocalPlayer().getBlockX() + width / 2, Wrapper.INSTANCE.getLocalPlayer().getBlockY() + height - 1, Wrapper.INSTANCE.getLocalPlayer().getBlockZ() + width / 2);
        switch (direction) {
            case NORTH -> box = box.offset(0, 0, -width / 2);
            case SOUTH -> box = box.offset(0, 0, width / 2);
            case EAST -> box = box.offset(width / 2, 0, 0);
            case WEST -> box = box.offset(-width / 2, 0, 0);
        }
        return box;
    }

    public double moveSpeed() {
        if (Speed.INSTANCE.getState()) {
            return getSpeedModSpeed();
        }
        return PlayerHelper.INSTANCE.getBaseMoveSpeed();
    }

    public double getSpeedModSpeed() {
        switch (Speed.INSTANCE.mode.toLowerCase()) {
            case "vanilla":
                return Speed.INSTANCE.vanillaSpeed;
            case "strafe":
                return Speed.INSTANCE.strafeSpeed;
        }
        return PlayerHelper.INSTANCE.getBaseMoveSpeed();
    }

    private String getDirectionString() {
        return switch (direction) {
            case NORTH -> "-Z";
            case SOUTH -> "+Z";
            case WEST -> "-X";
            case EAST -> "+X";
            default -> "";
        };
    }
}
