package me.dustin.jex.feature.mod.impl.render.storageesp.impl;

import java.awt.*;
import java.util.ArrayList;

import me.dustin.events.core.Event;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.storageesp.StorageESP;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.Render3DHelper.BoxStorage;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class BoxStorageESP extends FeatureExtension {
    private StorageESP storageESP;
    private final Box SINGLE_CHEST = new Box(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);
    public BoxStorageESP() {
        super("Box", StorageESP.class);
    }

    @Override
    public void pass(Event event) {
        if (storageESP == null) {
            storageESP = Feature.get(StorageESP.class);
        }
        if (event instanceof EventRender3D eventRender3D) {
            ArrayList<CustomBoxStorage> list = new ArrayList<>();
            ArrayList<BlockPos> chestPositions = new ArrayList<>();
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (storageESP.isValid(entity)) {
                    Vec3d renderPos = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                    Box box = WorldHelper.SINGLE_BOX.offset(renderPos).offset(-0.5, 0, -0.5);
                    list.add(new CustomBoxStorage(box, storageESP.getColor(entity), entity.getPos()));
                }
            });
            WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                if (storageESP.isValid(blockEntity)) {
                	Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getPos());
                    if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
                        if (chestPositions.contains(blockEntity.getPos()))
                            return;
                        Direction facingDir = WorldHelper.INSTANCE.chestMergeDirection(chestBlockEntity);
                        chestPositions.add(blockEntity.getPos().offset(facingDir));
                    }
                    Box box = getBox(blockEntity).offset(renderPos);
                    list.add(new CustomBoxStorage(box, storageESP.getColor(blockEntity), Vec3d.ofCenter(blockEntity.getPos())));
                }
            });

            Render3DHelper.INSTANCE.setup3DRender(true);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            list.forEach(blockStorage -> {
                Box box = blockStorage.box();
                Color alphaColor = new Color((int)255, (int)255, (int)255, !storageESP.fadeBoxesWhenClose ? 255 : Math.max(0, Math.min(255, (int)(ClientMathHelper.INSTANCE.getDistance(blockStorage.vec3d(), Wrapper.INSTANCE.getLocalPlayer().getPos()) - storageESP.fadeDistance) * 12)));
                int color = blockStorage.color();
                Render3DHelper.INSTANCE.drawOutlineBox(eventRender3D.getMatrixStack(), box, color & alphaColor.getRGB(), false);
            });
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);


            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            list.forEach(blockStorage -> {
                Box box = blockStorage.box();
                Color alphaColor = new Color((int)255, (int)255, (int)255, !storageESP.fadeBoxesWhenClose ? 100 : Math.max(0, Math.min(100, (int)(ClientMathHelper.INSTANCE.getDistance(blockStorage.vec3d(), Wrapper.INSTANCE.getLocalPlayer().getPos().add(0, Wrapper.INSTANCE.getLocalPlayer().getHeight() / 2, 0)) - storageESP.fadeDistance) * 12)));
                int color = blockStorage.color();
                Render3DHelper.INSTANCE.drawFilledBox(eventRender3D.getMatrixStack(), box, color & alphaColor.getRGB(), false);
            });
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);

            Render3DHelper.INSTANCE.end3DRender();
        }
    }

    public Box getBox(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
            Box box = SINGLE_CHEST;
            BlockState blockState = WorldHelper.INSTANCE.getBlockState(blockEntity.getPos());
            ChestBlock chestBlock = (ChestBlock) blockState.getBlock();
            if (blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                Box thisShape = chestBlock.getOutlineShape(blockState, Wrapper.INSTANCE.getWorld(), chestBlockEntity.getPos(), ShapeContext.absent()).getBoundingBox();
                Direction facingDir = WorldHelper.INSTANCE.chestMergeDirection(chestBlockEntity);
                if (facingDir == Direction.UP)
                    return box;
                BlockState connectedState = WorldHelper.INSTANCE.getBlockState(blockEntity.getPos().offset(facingDir));
                if (!(connectedState.getBlock() instanceof ChestBlock))
                    return box;
                Box connectionShape = chestBlock.getOutlineShape(connectedState, Wrapper.INSTANCE.getWorld(), chestBlockEntity.getPos().offset(facingDir), ShapeContext.absent()).getBoundingBox();
                box = VoxelShapes.union(VoxelShapes.cuboid(thisShape), VoxelShapes.cuboid(connectionShape).offset(Vec3d.of(BlockPos.ORIGIN.offset(facingDir)).x, Vec3d.of(BlockPos.ORIGIN.offset(facingDir)).y, Vec3d.of(BlockPos.ORIGIN.offset(facingDir)).z)).getBoundingBox();
            }
            return box;
        }
        if (blockEntity instanceof EnderChestBlockEntity)
            return SINGLE_CHEST;
        return WorldHelper.SINGLE_BOX;
    }

    public record CustomBoxStorage (Box box, int color, Vec3d vec3d) {}
}
