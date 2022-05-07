package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.priority.Priority;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.player.EventWalkOffBlock;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.Scaffold;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Place blocks under yourself automatically when walking on liquids.")
public class AntiLiquid extends Feature {

    @Op(name = "Place Mode", all = {"Post", "Pre"})
    public String placeMode = "Post";
    @Op(name = "Source Only")
    public boolean sourceOnly = false;
    @Op(name = "Sneak on Place")
    public boolean sneak = false;
    @Op(name = "Distance", min = 1, max = 5)
    public int distance = 3;
    @Op(name = "Allow Illegal Place")
    public boolean illegalPlace = true;
    private BlockPos pos;

    private ArrayList<BlockPos> list;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating)
            return;
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            pos = null;
            list = null;

            AABB box = new AABB(Wrapper.INSTANCE.getPlayer().getBlockX() - distance, Wrapper.INSTANCE.getPlayer().getBlockY() - distance, Wrapper.INSTANCE.getPlayer().getBlockZ() - distance, Wrapper.INSTANCE.getPlayer().getBlockX() + distance, Wrapper.INSTANCE.getPlayer().getBlockY() + distance, Wrapper.INSTANCE.getPlayer().getBlockZ() + distance);
            list = WorldHelper.INSTANCE.getBlocksInBox(box);
            list.sort(Comparator.comparingDouble(value -> ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().position(), Vec3.atCenterOf(value))));

            for (BlockPos blockPos : list) {
                if (blockPos != Wrapper.INSTANCE.getPlayer().blockPosition() && blockPos != Wrapper.INSTANCE.getPlayer().blockPosition().above() && canPlaceHere(blockPos) && isReplaceable(blockPos) && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().position().add(0, Wrapper.INSTANCE.getPlayer().getEyeHeight(Wrapper.INSTANCE.getPlayer().getPose()), 0), Vec3.atCenterOf(blockPos)) <= Wrapper.INSTANCE.getMultiPlayerGameMode().getPickRange()) {
                    List<Entity> list = Wrapper.INSTANCE.getWorld().getEntities(null, new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1));
                    boolean collides = !list.isEmpty();
                    if (collides)
                        continue;
                    Block block = WorldHelper.INSTANCE.getBlock(blockPos);
                    if (!(block instanceof LiquidBlock)) {
                        Wrapper.INSTANCE.getMultiPlayerGameMode().continueDestroyBlock(blockPos, Direction.UP);
                        return;
                    }
                    if (getBlockFromHotbar() == -1) {
                        if (InventoryHelper.INSTANCE.isHotbarFull() && getBlockFromInv() != -1) {
                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getPlayer().containerMenu, getBlockFromInv(), ClickType.SWAP, 8);
                        }else if (getBlockFromInv() != -1) {
                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getPlayer().containerMenu, getBlockFromInv() < 9 ? getBlockFromInv() + 36 : getBlockFromInv(), ClickType.QUICK_MOVE);
                        }
                    }
                    if (getBlockFromHotbar() != -1) {
                        pos = blockPos;
                        if (sneak) {
                            NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerCommandPacket(Wrapper.INSTANCE.getPlayer(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
                        }
                        event.setRotation(PlayerHelper.INSTANCE.rotateFromVec(PlayerHelper.INSTANCE.getPlacingLookPos(pos), Wrapper.INSTANCE.getPlayer()));
                        InventoryHelper.INSTANCE.setSlot(getBlockFromHotbar(), true, true);
                        if (placeMode.equalsIgnoreCase("Pre")) {
                            PlayerHelper.INSTANCE.placeBlockInPos(pos, InteractionHand.MAIN_HAND, illegalPlace);
                        }
                    }
                    return;
                }
            }
        } else if (pos != null) {
            if (placeMode.equalsIgnoreCase("Post"))
                PlayerHelper.INSTANCE.placeBlockInPos(pos, InteractionHand.MAIN_HAND, illegalPlace);
            if (sneak) {
                NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerCommandPacket(Wrapper.INSTANCE.getPlayer(), ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            }
        }
    }, Priority.FIRST);

    @EventPointer
    private final EventListener<EventWalkOffBlock> eventWalkOffBlockEventListener = new EventListener<>(event -> {
        if (WorldHelper.INSTANCE.getBlockBelowEntity(Wrapper.INSTANCE.getPlayer()) instanceof LiquidBlock)
            event.cancel();
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (list == null)
            return;
        boolean foundPlacing = false;
        ArrayList<Render3DHelper.BoxStorage> renderList = new ArrayList<>();
        for (BlockPos pos : list) {
            if (pos != Wrapper.INSTANCE.getPlayer().blockPosition() && pos != Wrapper.INSTANCE.getPlayer().blockPosition().above() && isReplaceable(pos) && ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getPlayer().position().add(0, Wrapper.INSTANCE.getPlayer().getEyeHeight(Wrapper.INSTANCE.getPlayer().getPose()), 0), Vec3.atCenterOf(pos)) <= Wrapper.INSTANCE.getMultiPlayerGameMode().getPickRange()) {
                Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(pos);
                AABB box = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                renderList.add(new Render3DHelper.BoxStorage(box, canPlaceHere(pos) && !foundPlacing ? 0xff00ff00 : 0xffff0000));
                if (canPlaceHere(pos))
                    foundPlacing = true;
            }
        }
        Render3DHelper.INSTANCE.setup3DRender(true);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        renderList.forEach(blockStorage -> {
            AABB box = blockStorage.box();
            int color = blockStorage.color();
            Render3DHelper.INSTANCE.drawOutlineBox(event.getPoseStack(), box, color, false);
        });
        bufferBuilder.clear();
        BufferUploader.drawWithShader(bufferBuilder.end());
        Render3DHelper.INSTANCE.end3DRender();
    });

    @Override
    public void onDisable() {
        pos = null;
        super.onDisable();
    }

    private boolean canPlaceHere(BlockPos pos) {
        return illegalPlace || getBlockInfo(pos) != null;
    }

    public Scaffold.BlockInfo getBlockInfo(BlockPos pos) {
        BlockPos down = pos.below();
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
        return (WorldHelper.INSTANCE.getBlock(blockPos) instanceof LiquidBlock fluidBlock && (!sourceOnly || fluidBlock.getFluidState(WorldHelper.INSTANCE.getBlockState(blockPos)).isSource())) || (WorldHelper.INSTANCE.isWaterlogged(blockPos) && !(WorldHelper.INSTANCE.getBlock(blockPos) instanceof LiquidBlock));
    }

    public int getBlockFromInv() {
        for (int i = 0; i < 36; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() instanceof BlockItem)
                if (shouldUse((BlockItem) InventoryHelper.INSTANCE.getInventory().getItem(i).getItem()))
                    return i;
        }
        return -1;
    }

    public int getBlockFromHotbar() {
        for (int i = 0; i < 9; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() instanceof BlockItem)
                if (shouldUse((BlockItem) InventoryHelper.INSTANCE.getInventory().getItem(i).getItem()))
                    return i;
        }
        return -1;
    }

    public boolean shouldUse(BlockItem blockItem) {
        return blockItem.getBlock().defaultBlockState().isCollisionShapeFullBlock(Wrapper.INSTANCE.getWorld(), BlockPos.ZERO) && blockItem.getBlock().defaultBlockState().use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) == InteractionResult.PASS;
    }

    public BlockPos getPos() {
        return pos;
    }
}
