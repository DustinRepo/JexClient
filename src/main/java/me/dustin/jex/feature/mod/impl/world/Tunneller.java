package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.movement.speed.Speed;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
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
        if (AutoEat.isEating || KillAura.INSTANCE.hasTarget())
            return;
        if (direction == null)
            direction = Wrapper.INSTANCE.getPlayer().getDirection();
        setSuffix(getDirectionString());
        //do liquid replacing
        if (handleLiquids)
            for (BlockPos liquidCheckSpot : getLiquidCheckSpots()) {
                if (WorldHelper.INSTANCE.getBlock(liquidCheckSpot) instanceof LiquidBlock) {
                    if (moveToBlocks()) {
                        PlayerHelper.INSTANCE.placeBlockInPos(liquidCheckSpot, InteractionHand.MAIN_HAND, true);
                        return;
                    } else {
                        ChatHelper.INSTANCE.addClientMessage("Tunneller ran out of blocks. Disabling.");
                        this.setState(false);
                        return;
                    }
                } else if (WorldHelper.INSTANCE.isWaterlogged(liquidCheckSpot)){
                    Wrapper.INSTANCE.getMultiPlayerGameMode().continueDestroyBlock(liquidCheckSpot, Direction.UP);
                    Wrapper.INSTANCE.getPlayer().swing(InteractionHand.MAIN_HAND);
                    return;
                }
            }
        //break-a da blocks
        for (BlockPos blockPos : getBlocksInTunnel()) {
            if (WorldHelper.INSTANCE.getBlockState(blockPos).getShape(Wrapper.INSTANCE.getWorld(), blockPos) != Shapes.empty()) {
                Wrapper.INSTANCE.getMultiPlayerGameMode().continueDestroyBlock(blockPos, Direction.UP);
                Wrapper.INSTANCE.getPlayer().swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
        //make sure floor is there
        AABB tunnelBox = getTunnelBox();
        AABB floorBox = new AABB(tunnelBox.minX, tunnelBox.minY - 1, tunnelBox.minZ, tunnelBox.maxX, tunnelBox.minY - 1, tunnelBox.maxZ);
        ArrayList<BlockPos> floorBlocks = WorldHelper.INSTANCE.getBlocksInBox(floorBox);
        for (BlockPos floorBlock : floorBlocks) {
            if (WorldHelper.INSTANCE.getBlockState(floorBlock).getShape(Wrapper.INSTANCE.getWorld(), floorBlock) == Shapes.empty()) {
                if (moveToBlocks()) {
                    PlayerHelper.INSTANCE.placeBlockInPos(floorBlock, InteractionHand.MAIN_HAND, true);
                } else {
                    ChatHelper.INSTANCE.addClientMessage("Tunneller ran out of blocks. Disabling.");
                    this.setState(false);
                }
                return;
            }
        }
        //move forward until one of the above catches
        switch (direction) {
            case NORTH -> PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), -moveSpeed());
            case SOUTH -> PlayerHelper.INSTANCE.setVelocityZ(Wrapper.INSTANCE.getPlayer(), moveSpeed());
            case WEST -> PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), -moveSpeed());
            case EAST -> PlayerHelper.INSTANCE.setVelocityX(Wrapper.INSTANCE.getPlayer(), moveSpeed());
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getPlayer() != null) {
            direction = Wrapper.INSTANCE.getPlayer().getDirection();
            PlayerHelper.INSTANCE.centerPerfectlyOnBlock();
        }
        super.onEnable();
    }

    private boolean moveToBlocks() {
        if (getBlockFromHotbar() == -1) {
            if (getBlockFromInv() != -1) {
                InventoryHelper.INSTANCE.swapToHotbar(getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), 8);
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
            if (InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() instanceof BlockItem blockItem)
                if (shouldUse(blockItem))
                    return i;
        }
        return -1;
    }

    public int getBlockFromHotbar() {
        for (int i = 0; i < 9; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() instanceof BlockItem blockItem)
                if (shouldUse(blockItem))
                    return i;
        }
        return -1;
    }

    public boolean shouldUse(BlockItem blockItem) {
        return blockItem.getBlock().defaultBlockState().entityCanStandOn(Wrapper.INSTANCE.getWorld(), BlockPos.ZERO, Wrapper.INSTANCE.getPlayer()) && blockItem.getBlock().defaultBlockState().use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS;
    }

    private ArrayList<BlockPos> getLiquidCheckSpots() {
        AABB box = getTunnelBox().inflate(1);
        ArrayList<BlockPos> blocks = WorldHelper.INSTANCE.getBlocksInBox(box);
        blocks.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().position(), Vec3.atCenterOf(value))));
        blocks.sort(Comparator.comparingInt(value -> -value.getY()));
        return blocks;
    }

    private ArrayList<BlockPos> getBlocksInTunnel() {
        AABB box = getTunnelBox();
        ArrayList<BlockPos> blocks = WorldHelper.INSTANCE.getBlocksInBox(box);
        blocks.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().position(), Vec3.atCenterOf(value))));
        blocks.sort(Comparator.comparingInt(value -> -value.getY()));
        return blocks;
    }

    private AABB getTunnelBox() {
        AABB box = new AABB(Wrapper.INSTANCE.getPlayer().getBlockX() - width / 2.f, Wrapper.INSTANCE.getPlayer().getBlockY(), Wrapper.INSTANCE.getPlayer().getBlockZ() - width / 2.f, Wrapper.INSTANCE.getPlayer().getBlockX() + width / 2.f, Wrapper.INSTANCE.getPlayer().getBlockY() + height - 1, Wrapper.INSTANCE.getPlayer().getBlockZ() + width / 2.f);
        switch (direction) {
            case NORTH -> box = box.move(0, 0, -width / 2.f);
            case SOUTH -> box = box.move(0, 0, width / 2.f);
            case EAST -> box = box.move(width / 2.f, 0, 0);
            case WEST -> box = box.move(-width / 2.f, 0, 0);
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
        return switch (Speed.INSTANCE.mode.toLowerCase()) {
            case "vanilla" -> Speed.INSTANCE.vanillaSpeed;
            case "strafe" -> Speed.INSTANCE.strafeSpeed;
            default -> PlayerHelper.INSTANCE.getBaseMoveSpeed();
        };
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
