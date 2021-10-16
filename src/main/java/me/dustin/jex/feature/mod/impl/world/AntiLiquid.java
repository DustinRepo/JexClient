package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Scaffold;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

@Feature.Manifest(name = "AntiLiquid", category = Feature.Category.WORLD, description = "Place blocks under yourself automatically when walking on liquids.")
public class AntiLiquid extends Feature {

    @Op(name = "Place Mode", all = {"Post", "Pre"})
    public String placeMode = "Post";
    @Op(name = "Sneak on Place")
    public boolean sneak = false;
    @Op(name = "Allow Illegal Place")
    public boolean illegalPlace = true;
    private BlockPos pos;

    @EventListener(events = {EventPlayerPackets.class, EventWalkOffBlock.class, EventRender3D.class})
    private void runMethod(Event event) {
        if (AutoEat.isEating)
            return;
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                pos = null;

                BlockPos origin = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
                if (WorldHelper.INSTANCE.getBlock(origin) == Blocks.SOUL_SAND || WorldHelper.INSTANCE.getBlock(origin) == Blocks.SOUL_SOIL)
                    origin = origin.up();
                ArrayList<BlockPos> list = new ArrayList<>();
                list.add(origin.down());
                list.add(origin.up().up());
                list.add(origin.up().north());
                list.add(origin.up().east());
                list.add(origin.up().south());
                list.add(origin.up().west());
                list.add(origin.north());
                list.add(origin.east());
                list.add(origin.south());
                list.add(origin.west());

                for (BlockPos blockPos : list) {
                    if (canPlaceHere(blockPos) && isReplaceable(WorldHelper.INSTANCE.getBlock(blockPos))) {
                        if (getBlockFromHotbar() == -1) {
                            if (InventoryHelper.INSTANCE.isHotbarFull()) {
                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 44, SlotActionType.THROW);
                            }
                            if (getBlockFromInv() != -1) {
                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), SlotActionType.QUICK_MOVE);
                            }
                        }
                        if (getBlockFromHotbar() != -1) {
                            pos = blockPos;
                            if (sneak) {
                                NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                            }
                            eventPlayerPackets.setRotation(PlayerHelper.INSTANCE.getRotations(PlayerHelper.INSTANCE.getPlacingLookPos(pos, illegalPlace), Wrapper.INSTANCE.getLocalPlayer()));
                            InventoryHelper.INSTANCE.setSlot(getBlockFromHotbar(), true, true);
                            if (placeMode.equalsIgnoreCase("Pre")) {
                                PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, illegalPlace);
                            }
                        }
                        return;
                    }
                }
            } else if (pos != null) {
                if (placeMode.equalsIgnoreCase("Post"))
                    PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, illegalPlace);
                if (sneak) {
                    NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                }
            }
        } else if (event instanceof EventWalkOffBlock) {
            if (WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getLocalPlayer()) instanceof FluidBlock)
                event.cancel();
        } else if (event instanceof EventRender3D eventRender3D) {
            BlockPos origin = new BlockPos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ());
            if (WorldHelper.INSTANCE.getBlock(origin) == Blocks.SOUL_SAND || WorldHelper.INSTANCE.getBlock(origin) == Blocks.SOUL_SOIL)
                origin = origin.up();
            ArrayList<BlockPos> list = new ArrayList<>();
            list.add(origin.down());
            list.add(origin.up().up());
            list.add(origin.up().north());
            list.add(origin.up().east());
            list.add(origin.up().south());
            list.add(origin.up().west());
            list.add(origin.north());
            list.add(origin.east());
            list.add(origin.south());
            list.add(origin.west());
            boolean foundPlacing = false;
            ArrayList<Render3DHelper.BoxStorage> renderList = new ArrayList<>();
            for (BlockPos pos : list) {
                if (isReplaceable(WorldHelper.INSTANCE.getBlock(pos))) {
                    Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(pos);
                    Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                    renderList.add(new Render3DHelper.BoxStorage(box, canPlaceHere(pos) && !foundPlacing ? 0xff00ff00 : 0xffff0000));
                    if (canPlaceHere(pos))
                        foundPlacing = true;
                }
            }
            Render3DHelper.INSTANCE.drawList(eventRender3D.getMatrixStack(), renderList);
        }
    }

    private boolean canPlaceHere(BlockPos pos) {
        return illegalPlace || getBlockInfo(pos) != null;
    }

    public Scaffold.BlockInfo getBlockInfo(BlockPos pos) {
        BlockPos down = pos.down();
        BlockPos north = pos.north();
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        if (!isReplaceable(WorldHelper.INSTANCE.getBlock(down))) {
            return new Scaffold.BlockInfo(pos, Direction.UP);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(north))) {
            return new Scaffold.BlockInfo(pos, Direction.SOUTH);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(east))) {
            return new Scaffold.BlockInfo(pos, Direction.WEST);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(south))) {
            return new Scaffold.BlockInfo(pos, Direction.NORTH);
        } else if (!isReplaceable(WorldHelper.INSTANCE.getBlock(west))) {
            return new Scaffold.BlockInfo(pos, Direction.EAST);
        }
        return null;
    }

    private boolean isReplaceable(Block block) {
        return block instanceof FluidBlock;
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
}
