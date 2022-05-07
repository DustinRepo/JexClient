package me.dustin.jex.feature.mod.impl.render.storageesp.impl;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.*;
import java.util.ArrayList;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.storageesp.StorageESP;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

public class BoxStorageESP extends FeatureExtension {
    private StorageESP storageESP;
    private final AABB SINGLE_CHEST = new AABB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);
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
            Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
                if (storageESP.isValid(entity)) {
                    float distance = ClientMathHelper.INSTANCE.getDistance(entity.position(), Wrapper.INSTANCE.getLocalPlayer().position());
                    if (storageESP.fadeBoxesWhenClose) {
                        if (distance < storageESP.fadeDistance)
                            return;
                    }

                    Vec3 renderPos = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, eventRender3D.getPartialTicks());
                    AABB box = WorldHelper.SINGLE_BOX.move(renderPos).move(-0.5, 0, -0.5);
                    list.add(new CustomBoxStorage(box, storageESP.getColor(entity), distance));
                }
            });
            WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                if (storageESP.isValid(blockEntity)) {
                	Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getBlockPos());
                    float distance = ClientMathHelper.INSTANCE.getDistance(Vec3.atCenterOf(blockEntity.getBlockPos()), Wrapper.INSTANCE.getLocalPlayer().position());
                    if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
                        if (chestPositions.contains(blockEntity.getBlockPos()))
                            return;
                        if (storageESP.fadeBoxesWhenClose) {
                            if (distance < storageESP.fadeDistance)
                                return;
                        }
                        Direction facingDir = WorldHelper.INSTANCE.chestMergeDirection(chestBlockEntity);
                        chestPositions.add(blockEntity.getBlockPos().relative(facingDir));
                    }
                    AABB box = getBox(blockEntity).move(renderPos);
                    list.add(new CustomBoxStorage(box, storageESP.getColor(blockEntity), distance));
                }
            });

            Render3DHelper.INSTANCE.setup3DRender(true);
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            list.forEach(blockStorage -> {
                AABB box = blockStorage.box();
                Color alphaColor = new Color((int)255, (int)255, (int)255, !storageESP.fadeBoxesWhenClose ? 255 : Math.max(0, Math.min(255, (int)(blockStorage.distance - storageESP.fadeDistance) * 12)));
                int color = blockStorage.color();
                Render3DHelper.INSTANCE.drawOutlineBox(eventRender3D.getPoseStack(), box, color & alphaColor.getRGB(), false);
            });
            bufferBuilder.clear();
            BufferUploader.drawWithShader(bufferBuilder.end());

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            list.forEach(blockStorage -> {
                AABB box = blockStorage.box();
                Color alphaColor = new Color((int)255, (int)255, (int)255, !storageESP.fadeBoxesWhenClose ? 100 : Math.max(0, Math.min(100, (int)(blockStorage.distance - storageESP.fadeDistance) * 12)));
                int color = blockStorage.color();
                Render3DHelper.INSTANCE.drawFilledBox(eventRender3D.getPoseStack(), box, color & alphaColor.getRGB(), false);
            });
            bufferBuilder.clear();
            BufferUploader.drawWithShader(bufferBuilder.end());
            Render3DHelper.INSTANCE.end3DRender();
        }
    }

    public AABB getBox(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
            AABB box = SINGLE_CHEST;
            BlockState blockState = WorldHelper.INSTANCE.getBlockState(blockEntity.getBlockPos());
            ChestBlock chestBlock = (ChestBlock) blockState.getBlock();
            if (blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                AABB thisShape = chestBlock.getShape(blockState, Wrapper.INSTANCE.getWorld(), chestBlockEntity.getBlockPos(), CollisionContext.empty()).bounds();
                Direction facingDir = WorldHelper.INSTANCE.chestMergeDirection(chestBlockEntity);
                if (facingDir == Direction.UP)
                    return box;
                BlockState connectedState = WorldHelper.INSTANCE.getBlockState(blockEntity.getBlockPos().relative(facingDir));
                if (!(connectedState.getBlock() instanceof ChestBlock))
                    return box;
                AABB connectionShape = chestBlock.getShape(connectedState, Wrapper.INSTANCE.getWorld(), chestBlockEntity.getBlockPos().relative(facingDir), CollisionContext.empty()).bounds();
                box = Shapes.or(Shapes.create(thisShape), Shapes.create(connectionShape).move(Vec3.atLowerCornerOf(BlockPos.ZERO.relative(facingDir)).x, Vec3.atLowerCornerOf(BlockPos.ZERO.relative(facingDir)).y, Vec3.atLowerCornerOf(BlockPos.ZERO.relative(facingDir)).z)).bounds();
            }
            return box;
        }
        if (blockEntity instanceof EnderChestBlockEntity)
            return SINGLE_CHEST;
        return WorldHelper.SINGLE_BOX;
    }

    public record CustomBoxStorage (AABB box, int color, double distance) {}
}
