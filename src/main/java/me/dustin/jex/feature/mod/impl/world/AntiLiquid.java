package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Scaffold;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AntiLiquid extends Feature {

    public final Property<PlaceMode> placeModeProperty = new Property.PropertyBuilder<PlaceMode>(this.getClass())
            .name("Place Mode")
            .description("Timing to place the blocks.")
            .value(PlaceMode.POST)
            .build();
    public final Property<Boolean> sourceOnlyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Source Only")
            .value(true)
            .build();
    public final Property<Boolean> sneakProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Sneak on Place")
            .value(false)
            .build();
    public final Property<Integer> distanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Distance")
            .value(3)
            .min(1)
            .max(5)
            .build();
    public final Property<Boolean> illegalPlaceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Allow Illegal Place")
            .description("Allow placements without having a side of a block to use.")
            .value(true)
            .build();

    private BlockPos pos;

    private ArrayList<BlockPos> list;

    public AntiLiquid() {
        super(Category.WORLD, "Place blocks under yourself automatically when walking on liquids.");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating)
            return;
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            pos = null;
            list = null;

            Box box = new Box(Wrapper.INSTANCE.getPlayer().getBlockX() - distanceProperty.value(), Wrapper.INSTANCE.getPlayer().getBlockY() - distanceProperty.value(), Wrapper.INSTANCE.getPlayer().getBlockZ() - distanceProperty.value(), Wrapper.INSTANCE.getPlayer().getBlockX() + distanceProperty.value(), Wrapper.INSTANCE.getPlayer().getBlockY() + distanceProperty.value(), Wrapper.INSTANCE.getPlayer().getBlockZ() + distanceProperty.value());
            list = WorldHelper.INSTANCE.getBlocksInBox(box);
            list.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos(), Vec3d.ofCenter(value))));

            for (BlockPos blockPos : list) {
                if (blockPos != Wrapper.INSTANCE.getPlayer().getBlockPos() && blockPos != Wrapper.INSTANCE.getPlayer().getBlockPos().up() && canPlaceHere(blockPos) && isReplaceable(blockPos) && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos().add(0, Wrapper.INSTANCE.getPlayer().getEyeHeight(Wrapper.INSTANCE.getPlayer().getPose()), 0), Vec3d.ofCenter(blockPos)) <= Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance()) {
                    List<Entity> list = Wrapper.INSTANCE.getWorld().getOtherEntities(null, new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1));
                    boolean collides = !list.isEmpty();
                    if (collides)
                        continue;
                    Block block = WorldHelper.INSTANCE.getBlock(blockPos);
                    if (!(block instanceof FluidBlock)) {
                        Wrapper.INSTANCE.getClientPlayerInteractionManager().updateBlockBreakingProgress(blockPos, Direction.UP);
                        return;
                    }
                    if (getBlockFromHotbar() == -1) {
                        if (InventoryHelper.INSTANCE.isHotbarFull() && getBlockFromInv() != -1) {
                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getPlayer().currentScreenHandler, getBlockFromInv(), SlotActionType.SWAP, 8);
                        }else if (getBlockFromInv() != -1) {
                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getPlayer().currentScreenHandler, getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), SlotActionType.QUICK_MOVE);
                        }
                    }
                    if (getBlockFromHotbar() != -1) {
                        pos = blockPos;
                        if (sneakProperty.value()) {
                            NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getPlayer(), ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                        }
                        event.setRotation(PlayerHelper.INSTANCE.rotateFromVec(PlayerHelper.INSTANCE.getPlacingLookPos(pos), Wrapper.INSTANCE.getPlayer()));
                        InventoryHelper.INSTANCE.setSlot(getBlockFromHotbar(), true, true);
                        if (placeModeProperty.value() == PlaceMode.PRE) {
                            PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, illegalPlaceProperty.value());
                        }
                    }
                    return;
                }
            }
        } else if (pos != null) {
            if (placeModeProperty.value() == PlaceMode.POST)
                PlayerHelper.INSTANCE.placeBlockInPos(pos, Hand.MAIN_HAND, illegalPlaceProperty.value());
            if (sneakProperty.value()) {
                NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
        }
    }, Priority.FIRST);

    @EventPointer
    private final EventListener<EventWalkOffBlock> eventWalkOffBlockEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getPlayer()) instanceof FluidBlock)
            event.cancel();
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (list == null)
            return;
        boolean foundPlacing = false;
        ArrayList<Render3DHelper.BoxStorage> renderList = new ArrayList<>();
        for (BlockPos pos : list) {
            if (pos != Wrapper.INSTANCE.getPlayer().getBlockPos() && pos != Wrapper.INSTANCE.getPlayer().getBlockPos().up() && isReplaceable(pos) && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().getPos().add(0, Wrapper.INSTANCE.getPlayer().getEyeHeight(Wrapper.INSTANCE.getPlayer().getPose()), 0), Vec3d.ofCenter(pos)) <= Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance()) {
                Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(pos);
                Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                renderList.add(new Render3DHelper.BoxStorage(box, canPlaceHere(pos) && !foundPlacing ? 0xff00ff00 : 0xffff0000));
                if (canPlaceHere(pos))
                    foundPlacing = true;
            }
        }
        Render3DHelper.INSTANCE.setup3DRender(true);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        renderList.forEach(blockStorage -> {
            Box box = blockStorage.box();
            int color = blockStorage.color();
            Render3DHelper.INSTANCE.drawOutlineBox(event.getPoseStack(), box, color, false);
        });
        bufferBuilder.clear();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        Render3DHelper.INSTANCE.end3DRender();
    });

    @Override
    public void onDisable() {
        pos = null;
        super.onDisable();
    }

    private boolean canPlaceHere(BlockPos pos) {
        return illegalPlaceProperty.value() || getBlockInfo(pos) != null;
    }

    public Scaffold.BlockInfo getBlockInfo(BlockPos pos) {
        BlockPos down = pos.down();
        BlockPos north = pos.north();
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        if (!isReplaceable(down)) {
            return new Scaffold.BlockInfo(pos, Direction.UP);
        } else if (!isReplaceable(north)) {
            return new Scaffold.BlockInfo(pos, Direction.SOUTH);
        } else if (!isReplaceable(east)) {
            return new Scaffold.BlockInfo(pos, Direction.WEST);
        } else if (!isReplaceable(south)) {
            return new Scaffold.BlockInfo(pos, Direction.NORTH);
        } else if (!isReplaceable(west)) {
            return new Scaffold.BlockInfo(pos, Direction.EAST);
        }
        return null;
    }

    private boolean isReplaceable(BlockPos blockPos) {
        return (WorldHelper.INSTANCE.getBlock(blockPos) instanceof FluidBlock fluidBlock && (!sourceOnlyProperty.value() || fluidBlock.getFluidState(WorldHelper.INSTANCE.getBlockState(blockPos)).isStill())) || (WorldHelper.INSTANCE.isWaterlogged(blockPos) && !(WorldHelper.INSTANCE.getBlock(blockPos) instanceof FluidBlock));
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
        return blockItem.getBlock().getDefaultState().isFullCube(Wrapper.INSTANCE.getWorld(), BlockPos.ORIGIN) && blockItem.getBlock().getDefaultState().onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) == ActionResult.PASS;
    }

    public BlockPos getPos() {
        return pos;
    }

    public enum PlaceMode {
        PRE, POST
    }
}
